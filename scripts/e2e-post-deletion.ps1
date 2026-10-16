param(
    [string]$BaseUrl = 'http://127.0.0.1:8087',
    [string]$ComposeProject = 'pookyblog-e2e-20261016',
    [string]$ComposeOverride = 'web/build/e2e-logs/compose.e2e.yaml'
)

$ErrorActionPreference = 'Stop'
if (-not $ComposeProject.StartsWith('pookyblog-e2e-') -or -not (Test-Path -LiteralPath $ComposeOverride)) {
    throw 'An isolated pookyblog-e2e-* Compose project and its override are required.'
}
$compose = @('compose', '-p', $ComposeProject, '-f', 'compose.yaml', '-f', $ComposeOverride)

function Invoke-App([string]$Method, [string]$Path, $Body, $Session) {
    $request = @{ Uri = $BaseUrl + $Path; Method = $Method; WebSession = $Session; UseBasicParsing = $true; TimeoutSec = 30; ErrorAction = 'Stop' }
    if ($null -ne $Body) { $request.ContentType = 'application/json'; $request.Body = $Body | ConvertTo-Json -Compress }
    try { $response = Invoke-WebRequest @request; return @{ Code = [int]$response.StatusCode; Body = [string]$response.Content } }
    catch {
        $response = $_.Exception.Response
        if ($null -eq $response) { throw }
        $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
        return @{ Code = [int]$response.StatusCode; Body = $reader.ReadToEnd() }
    }
}
function Assert-Code($Response, [int]$Expected, [string]$Step) {
    if ($Response.Code -ne $Expected) { throw "$Step returned HTTP $($Response.Code); expected $Expected." }
}
function Query-Count([string]$Table, [long]$PostId) {
    if ($Table -notin @('post_like', 'comment')) { throw 'Unexpected table name.' }
    $result = "SELECT COUNT(*) FROM $Table WHERE post_id = $PostId;" |
        & docker @compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root -D pookydb -N'
    if ($LASTEXITCODE -ne 0) { throw "Database count query failed for $Table." }
    return [int]($result | Select-Object -Last 1)
}
function Redis-Result([string[]]$RedisArgs) {
    $result = & docker @compose exec -T redis redis-cli @RedisArgs
    if ($LASTEXITCODE -ne 0) { throw 'Isolated Redis read failed.' }
    return [string]($result | Select-Object -Last 1)
}
function Wait-Until([scriptblock]$Condition, [string]$Step) {
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if (& $Condition) { return }
        Start-Sleep -Seconds 1
    }
    throw "$Step did not complete within 30 seconds."
}

$suffix = [guid]::NewGuid().ToString('N').Substring(0, 8)
$password = [guid]::NewGuid().ToString('N') + 'Aa1!'
$sessions = @()
for ($i = 0; $i -lt 3; $i++) {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $username = "e2edel$suffix$i"
    Assert-Code (Invoke-App 'POST' '/api/auth/signup' @{
        username = $username; password = $password; nickname = "del$suffix$i"; email = "$username@example.invalid"
    } $session) 200 "signup user $i"
    Assert-Code (Invoke-App 'POST' '/api/auth/login' @{ username = $username; password = $password } $session) 200 "login user $i"
    $sessions += $session
}
Write-Output 'signup/login: PASS (3 isolated test users)'

foreach ($likeTotal in @(0, 1, 3)) {
    $title = "E2E delete $suffix likes $likeTotal"
    Assert-Code (Invoke-App 'POST' '/api/posts' @{ title = $title; content = "Deletion test $suffix" } $sessions[0]) 200 'create post'
    $list = Invoke-App 'GET' '/api/posts?page=1&size=20' $null $sessions[0]
    Assert-Code $list 200 'post list'
    $posts = ConvertFrom-Json -InputObject $list.Body
    $post = @($posts) | Where-Object { $_.title -eq $title } | Select-Object -First 1
    if ($null -eq $post) { throw 'Created post was not found in the list.' }
    $postId = [long]$post.id

    for ($i = 0; $i -lt $likeTotal; $i++) {
        Assert-Code (Invoke-App 'POST' "/api/posts/$postId/likes" $null $sessions[$i]) 200 "like $i"
    }
    if ($likeTotal -eq 3) {
        Assert-Code (Invoke-App 'DELETE' "/api/posts/$postId/likes" $null $sessions[2]) 200 'unlike regression'
        Assert-Code (Invoke-App 'POST' "/api/posts/$postId/likes" $null $sessions[2]) 200 'relike regression'
        Assert-Code (Invoke-App 'POST' "/api/posts/$postId/comments" @{ comment = "Cascade comment $suffix" } $sessions[0]) 200 'comment create'
    }
    $likesBefore = Invoke-App 'GET' "/api/posts/$postId/likes/count" $null $sessions[0]
    Assert-Code $likesBefore 200 'like count before deletion'
    if ([int]$likesBefore.Body.Trim() -ne $likeTotal) { throw 'Like count before deletion is wrong.' }

    if ($likeTotal -eq 3) {
        $day = Get-Date -Format yyyyMMdd
        Wait-Until { (Redis-Result @('ZSCORE', "hot-post::list::$day", "$postId")) -ne '' } 'hot-post ranking before deletion'
        Wait-Until { (Redis-Result @('EXISTS', "post-read::post::$postId")) -eq '1' } 'post-read creation consumer'
    }

    Assert-Code (Invoke-App 'DELETE' "/api/posts/$postId" $null $sessions[0]) 200 "delete post with $likeTotal likes"
    Assert-Code (Invoke-App 'GET' "/posts/getPost/$postId" $null $sessions[0]) 404 'deleted post detail'
    if ((Query-Count 'post_like' $postId) -ne 0) { throw 'Like rows remained after post deletion.' }
    if ((Query-Count 'comment' $postId) -ne 0) { throw 'Comment rows remained after post deletion.' }
    $likesAfter = Invoke-App 'GET' "/api/posts/$postId/likes/count" $null $sessions[0]
    Assert-Code $likesAfter 200 'like count after deletion'
    if ([int]$likesAfter.Body.Trim() -ne 0) { throw 'Like count remained after post deletion.' }
    if ($likeTotal -eq 3) {
        Wait-Until { (Redis-Result @('ZSCORE', "hot-post::list::$day", "$postId")) -eq '' } 'hot-post deletion consumer'
        Wait-Until { (Redis-Result @('EXISTS', "post-read::post::$postId")) -eq '0' } 'post-read deletion consumer'
    }
    Write-Output "delete with $likeTotal likes: PASS (HTTP 200, detail 404, no Like/Comment rows)"
}
Write-Output 'PostDeleted consumers and Like/Unlike regression: PASS'
