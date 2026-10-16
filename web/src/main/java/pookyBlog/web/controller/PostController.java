package pookyBlog.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import pookyBlog.common.Dto.Request.PostSearch;
import pookyBlog.common.Dto.Request.PostViewCountsRequest;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.common.Dto.Response.PostListResponse;
import pookyBlog.common.Dto.Response.PostListViewResponse;
import pookyBlog.common.Dto.Response.PostViewCountsResponse;
import pookyBlog.common.Dto.Response.CommentResponse;
import pookyBlog.common.Dto.Response.AuthMeResponse;
import pookyBlog.common.Entity.User;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PostController {

    // 각 서비스 API를 호출하기 위한 WebClient 주입
    private final WebClient postWebClient;
    private final WebClient userWebClient;
    private final WebClient commentWebClient;
    private final WebClient likeWebClient;
    private final WebClient viewWebClient;
    private final WebClient hotPostWebClient;

    public record HotPostSummary(Long postId, String title, LocalDateTime createAt) {}

    /**
     * 메인 페이지: 게시글 목록을 페이징하여 보여주고, 로그인 상태를 표시합니다.
     */
    @GetMapping("/")
    public String index(Model model, @ModelAttribute PostSearch postSearch, @CookieValue(name = "jwtToken", required = false) String token) {
        log.debug("Accessing index page with search: {}", postSearch);
        int currentPage = postSearch.getPage() == null ? 1 : Math.max(1, postSearch.getPage());
        int pageSize = postSearch.getSize() == null ? 10 : Math.max(1, Math.min(499, postSearch.getSize()));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("hasPrev", currentPage > 1);
        model.addAttribute("previous", currentPage - 1);
        model.addAttribute("showPagination", currentPage > 1);

        try {
            // 목록 API에 전체 페이지 수가 없으므로 같은 크기의 다음 페이지를 조회해 이동 가능 여부만 확인합니다.
            List<PostListResponse> posts = postWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/posts")
                            .queryParam("page", currentPage)
                            .queryParam("size", pageSize)
                            .build())
                    .retrieve()
                    .bodyToFlux(PostListResponse.class)
                    .collectList()
                    .block(); // 템플릿 렌더링을 위해 동기적으로 결과를 기다립니다.
            boolean hasNext = !posts.isEmpty() && posts.size() == pageSize && !postWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/posts")
                            .queryParam("page", currentPage + 1)
                            .queryParam("size", pageSize)
                            .build())
                    .retrieve()
                    .bodyToFlux(PostListResponse.class)
                    .collectList()
                    .block()
                    .isEmpty();
            model.addAttribute("hasNext", hasNext);
            model.addAttribute("showPagination", currentPage > 1 || hasNext);
            model.addAttribute("next", currentPage + 1);

            List<PostListViewResponse> postsWithViews;
            if (posts.isEmpty()) {
                postsWithViews = List.of();
            } else {
                PostViewCountsResponse viewCounts = viewWebClient.post()
                        .uri("/post-view/counts")
                        .headers(headers -> setBearerAuth(headers, token))
                        .bodyValue(new PostViewCountsRequest(posts.stream().map(PostListResponse::getId).toList()))
                        .retrieve()
                        .bodyToMono(PostViewCountsResponse.class)
                        .block();
                postsWithViews = posts.stream()
                        .map(post -> new PostListViewResponse(post, viewCounts.counts().get(post.getId())))
                        .toList();
            }

            model.addAttribute("posts", postsWithViews);

        } catch (WebClientResponseException e) {
            log.error("Failed to fetch posts from post-service. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("posts", List.of());
            model.addAttribute("listLoadError", true);
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching posts: {}", e.getMessage());
            model.addAttribute("posts", List.of());
            model.addAttribute("listLoadError", true);
        }

        try {
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            List<HotPostSummary> hotPosts = hotPostWebClient.get()
                    .uri("/hot-posts/date/{dateStr}", today)
                    .retrieve()
                    .bodyToFlux(HotPostSummary.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(4))
                    .block();
            List<Map<String, Object>> trending = java.util.stream.IntStream.range(0, Math.min(5, hotPosts.size()))
                    .mapToObj(index -> Map.<String, Object>of(
                            "rank", String.format("%02d", index + 1),
                            "postId", hotPosts.get(index).postId(),
                            "title", hotPosts.get(index).title()))
                    .toList();
            model.addAttribute("trending", trending);
        } catch (Exception e) {
            log.warn("Failed to fetch trending posts: {}", e.getMessage());
            model.addAttribute("trendingUnavailable", true);
        }

        // 로그인한 사용자 정보를 모델에 추가
        addUserInfoToModel(model, token);

        return "index";
    }

    /**
     * 글쓰기 페이지로 이동합니다.
     */
    @GetMapping("/posts/write")
    public String writePostForm(Model model, @CookieValue(name = "jwtToken", required = false) String token) {
        // 글쓰기 페이지에서는 로그인한 사용자 정보가 필요 (예: 작성자 이름 자동 완성)
        addUserInfoToModel(model, token);
        return "posts/posts-write";
    }

    /**
     * 게시글 상세 페이지: 게시글, 댓글, 좋아요, 조회수 정보를 각 서비스에서 가져와 조합합니다.
     */
    @GetMapping("/posts/getPost/{id}")
    public String getPost(@PathVariable Long id, Model model, @CookieValue(name = "jwtToken", required = false) String token) {
        log.debug("Accessing post detail page for id: {}", id);

        // 로그인한 사용자 정보를 모델에 먼저 추가
        addUserInfoToModel(model, token);

        try {
            // 1. 병렬 API 호출을 위한 Mono 준비
            Mono<PostResponse> postMono = postWebClient.get().uri("/posts/{postId}", id)
                    .headers(headers -> setBearerAuth(headers, token))
                    .retrieve().bodyToMono(PostResponse.class);
            Mono<List<CommentResponse>> commentsMono = commentWebClient.get().uri("/api/posts/{postId}", id)
                    .headers(headers -> setBearerAuth(headers, token))
                    .retrieve().bodyToMono(new ParameterizedTypeReference<>() {});
            Mono<Long> likesMono = likeWebClient.get().uri("/likes/count/{postId}", id)
                    .headers(headers -> setBearerAuth(headers, token))
                    .retrieve().bodyToMono(Long.class);
            Mono<Long> viewsMono = viewWebClient.get().uri("/post-view/{postId}/count", id)
                    .headers(headers -> setBearerAuth(headers, token))
                    .retrieve().bodyToMono(Long.class);

            // 2. 모든 Mono가 완료될 때까지 기다렸다가 결과를 한 번에 처리
            Mono.zip(postMono, commentsMono, likesMono, viewsMono)
                    .doOnSuccess(tuple -> {
                        PostResponse post = tuple.getT1();
                        model.addAttribute("posts", post);
                        model.addAttribute("comments", tuple.getT2());
                        model.addAttribute("commentCount", tuple.getT2().size());
                        model.addAttribute("likeCount", tuple.getT3());
                        model.addAttribute("viewCount", tuple.getT4());

                        // 작성자 여부 판단
                        User loginUser = (User) model.getAttribute("user");
                        if (loginUser != null && post.getWriter() != null && post.getWriter().equals(loginUser.getNickname())) {
                            model.addAttribute("writer", true);
                        } else {
                            model.addAttribute("writer", false);
                        }
                    }).block();

            User loginUser = (User) model.getAttribute("user");
            if (loginUser != null) {
                try {
                    Boolean liked = likeWebClient.get()
                            .uri(uriBuilder -> uriBuilder.path("/likes/exists/{postId}")
                                    .queryParam("userId", loginUser.getId())
                                    .build(id))
                            .headers(headers -> setBearerAuth(headers, token))
                            .retrieve().bodyToMono(Boolean.class)
                            .timeout(Duration.ofSeconds(2)).block();
                    if (liked == null) {
                        model.addAttribute("likeStateUnavailable", true);
                    } else {
                        model.addAttribute("liked", liked);
                        model.addAttribute("notLiked", !liked);
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch like state for post {}: {}", id, e.getMessage());
                    model.addAttribute("likeStateUnavailable", true);
                }
            }

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Post not found for id: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.", e);
        } catch (Exception e) {
            log.error("Failed to fetch post details for id {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 상세 조회에 실패했습니다.", e);
        }

        return "posts/posts-read";
    }

    /**
     * 글 수정 페이지로 이동합니다.
     */
    @GetMapping("/posts/update/{id}")
    public String updatePostForm(Model model, @PathVariable Long id,
                                 @CookieValue(name = "jwtToken", required = false) String token) {
        addUserInfoToModel(model, token);
        try {
            PostResponse postResponse = postWebClient.get()
                    .uri("/posts/{postId}", id)
                    .retrieve()
                    .bodyToMono(PostResponse.class)
                    .block();

            model.addAttribute("posts", postResponse);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Post not found for update with id: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.", e);
        } catch (Exception e) {
            log.error("Failed to fetch post for update with id {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 조회에 실패했습니다.", e);
        }

        return "posts/posts-update";
    }

    /**
     * [공통 메소드]
     * JWT 토큰을 이용해 user-service에서 사용자 정보를 조회하고 Model에 추가합니다.
     * @param model 뷰에 데이터를 전달할 Model 객체
     * @param token 브라우저 쿠키에서 가져온 JWT 토큰
     */
    private void addUserInfoToModel(Model model, String token) {
        if (token == null || token.isBlank()) {
            return; // 토큰이 없으면 비로그인 상태이므로 아무 작업도 하지 않음
        }

        try {
            // user-service에 "내 정보 조회" API가 있다고 가정 (예: /api/users/me)
            AuthMeResponse authenticatedUser = userWebClient.get()
                    .uri("/auth/me")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(AuthMeResponse.class)
                    .block();

            User user = User.builder()
                    .id(authenticatedUser.id())
                    .username(authenticatedUser.username())
                    .nickname(authenticatedUser.nickname())
                    .build();

            model.addAttribute("user", user);
            model.addAttribute("loggedIn", true);

        } catch (WebClientResponseException e) {
            // 4xx, 5xx 에러 (토큰 만료, 비유효 등)
            log.warn("Failed to get user info with token. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            // 네트워크 오류 등 기타 예외
            log.error("An unexpected error occurred while fetching user info: {}", e.getMessage());
        }
    }

    private void setBearerAuth(org.springframework.http.HttpHeaders headers, String token) {
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
    }
}
