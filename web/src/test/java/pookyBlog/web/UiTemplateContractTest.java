package pookyBlog.web;

import com.samskivert.mustache.Mustache;
import org.junit.jupiter.api.Test;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.common.Dto.Response.PostListResponse;
import pookyBlog.common.Dto.Response.PostListViewResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UiTemplateContractTest {

    private static final List<String> UI_RESOURCES = List.of(
            "templates/index.mustache",
            "templates/comment/form.mustache",
            "templates/comment/list.mustache",
            "templates/layout/header.mustache",
            "templates/layout/footer.mustache",
            "templates/posts/posts-page.mustache",
            "templates/posts/posts-read.mustache",
            "templates/posts/posts-update.mustache",
            "templates/posts/posts-write.mustache",
            "templates/user/user-login.mustache",
            "templates/user/user-signup.mustache",
            "static/js/app.js",
            "static/css/app.css"
    );

    @Test
    void uiResourcesPreserveUtf8KoreanWithoutReplacementText() throws IOException {
        assertThat(resource("templates/index.mustache"))
                .contains("게시글", "개발 경험과 생각을 자유롭게 나눠보세요");
        assertThat(resource("templates/layout/header.mustache"))
                .contains("홈", "로그인", "회원가입");
        assertThat(resource("templates/posts/posts-write.mustache"))
                .contains("새 글 작성", "제목", "내용");
        assertThat(resource("templates/user/user-login.mustache"))
                .contains("로그인", "아이디", "비밀번호");
        assertThat(resource("static/js/app.js"))
                .contains("로그인되었습니다", "회원가입이 완료되었습니다");

        for (String path : UI_RESOURCES) {
            assertThat(resource(path)).as(path).doesNotContain("???", "\uFFFD");
        }
    }

    @Test
    void commonHeadDeclaresUtf8Charset() throws IOException {
        assertThat(resource("templates/layout/header.mustache"))
                .contains("<meta charset=\"UTF-8\">");
    }

    @Test
    void layoutUsesBootstrapFiveAndResponsiveViewport() throws IOException {
        String header = resource("templates/layout/header.mustache");
        String footer = resource("templates/layout/footer.mustache");
        String css = resource("static/css/app.css");

        assertThat(header).contains("bootstrap@5.3.3", "width=device-width", "id=\"main-content\"");
        assertThat(footer).contains("bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js");
        assertThat(css).contains("@media (max-width: 767.98px)", "min-height: 100vh")
                .doesNotContain("height: 150%", "position: fixed;\n    width: 100%;\n    height: 50px");
    }

    @Test
    void interactiveSelectorsRemainConnectedToJavascript() throws IOException {
        String templates = resource("templates/posts/posts-read.mustache")
                + resource("templates/posts/posts-write.mustache")
                + resource("templates/posts/posts-update.mustache")
                + resource("templates/comment/form.mustache")
                + resource("templates/user/user-login.mustache")
                + resource("templates/user/user-signup.mustache");
        String javascript = resource("static/js/app.js");

        for (String id : new String[]{"btn-save", "btn-update", "btn-delete", "btn-login",
                "btn-signup", "btn-comment-save", "btn-like", "btn-unlike"}) {
            assertThat(templates).contains("id=\"" + id + "\"");
            assertThat(javascript).contains("$('#" + id + "')");
        }
    }

    @Test
    void indexRendersWithEmptyPostList() {
        assertThat(renderIndex(List.of())).contains("아직 작성된 게시글이 없습니다.");
    }

    @Test
    void indexRendersPostIndexResponseFields() {
        PostListViewResponse post = new PostListViewResponse(
                new PostListResponse(1L, "계약 테스트", "작성자", "2026.08.21"), 3L);

        assertThat(renderIndex(List.of(post)))
                .contains("계약 테스트", "작성자", "2026.08.21")
                .doesNotContain("아직 작성된 게시글이 없습니다.");
    }

    @Test
    void indexPostLinkMatchesDetailPageControllerMapping() throws IOException {
        assertThat(resource("templates/index.mustache"))
                .contains("href=\"/posts/getPost/{{id}}\"");
    }

    @Test
    void mutationUiShowsSuccessOnlyForSuccessfulAjaxResponses() throws IOException {
        String javascript = resource("static/js/app.js");
        for (String operation : new String[]{"updatePost", "deletePost", "commentSave", "like", "unlike"}) {
            assertThat(javascript).contains(operation + "(");
        }
        assertThat(javascript).contains(".done(", ".fail(error =>");
    }

    @Test
    void detailCountEndpointsAndDomSelectorsShareOneContract() throws IOException {
        String template = resource("templates/posts/posts-read.mustache");
        String javascript = resource("static/js/app.js");

        assertThat(template).contains("id=\"like-count\"", "id=\"view-count\"");
        assertThat(javascript)
                .contains("/likes/count", "/views/count")
                .contains("this.renderCount('#like-count', count)")
                .contains("this.renderCount('#view-count', count)")
                .contains(".done(() => this.refreshViewCount(id))");
    }

    private String renderIndex(List<PostListViewResponse> posts) {
        Mustache.Compiler compiler = Mustache.compiler().withLoader(name -> {
            var input = getClass().getClassLoader()
                    .getResourceAsStream("templates/" + name + ".mustache");
            if (input == null) {
                throw new IOException("Missing partial: " + name);
            }
            return new InputStreamReader(input, StandardCharsets.UTF_8);
        });
        return compiler.compile(resourceUnchecked("templates/index.mustache"))
                .execute(Map.of(
                        "posts", posts,
                        "hasPrev", false,
                        "hasNext", false
                ));
    }

    private String resourceUnchecked(String path) {
        try {
            return resource(path);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String resource(String path) throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
