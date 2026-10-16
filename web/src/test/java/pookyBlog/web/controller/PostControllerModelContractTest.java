package pookyBlog.web.controller;

import com.samskivert.mustache.Mustache;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import pookyBlog.common.Dto.Request.PostSearch;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.common.Dto.Response.PostListResponse;
import pookyBlog.common.Dto.Response.PostListViewResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostControllerModelContractTest {

    @Test
    void indexUsesRealNextPageAndKeepsPageSizeStable() {
        String firstPage = "[" + IntStream.rangeClosed(1, 10)
                .mapToObj(id -> "{\"id\":" + id + ",\"title\":\"post\",\"writer\":\"author\",\"createdDate\":\"2026.08.21\"}")
                .reduce((left, right) -> left + "," + right).orElseThrow() + "]";
        AtomicInteger postRequests = new AtomicInteger();
        WebClient postClient = WebClient.builder().exchangeFunction(request -> {
            postRequests.incrementAndGet();
            assertThat(request.url().getQuery()).contains("size=10");
            String body = request.url().getQuery().contains("page=2")
                    ? "[{\"id\":11,\"title\":\"next\",\"writer\":\"author\",\"createdDate\":\"2026.08.21\"}]"
                    : firstPage;
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json").body(body).build());
        }).build();
        WebClient unused = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new AssertionError("Unexpected request"))).build();
        WebClient views = jsonClient(new AtomicReference<>(), "{\"counts\":{}}");
        PostController controller = new PostController(postClient, unused, unused, unused, views, jsonClient(new AtomicReference<>(), "[]"));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.index(model, new PostSearch(), null);

        assertThat(postRequests).hasValue(2);
        assertThat(model.get("hasNext")).isEqualTo(true);
        assertThat(model.get("hasPrev")).isEqualTo(false);
        assertThat(model.get("currentPage")).isEqualTo(1);
        assertThat(((List<?>) model.get("posts"))).hasSize(10);
        assertThat(render(model)).contains("?page=2&amp;size=10#recent-posts", "1페이지");
    }

    @Test
    void indexCombinesThreeRedisCountsWithOneBatchRequest() {
        String postsJson = """
                [{"id":1,"title":"one","writer":"a","createdDate":"2026.08.21"},
                 {"id":2,"title":"two","writer":"b","createdDate":"2026.08.21"},
                 {"id":3,"title":"three","writer":"c","createdDate":"2026.08.21"}]
                """;
        AtomicInteger viewRequests = new AtomicInteger();
        WebClient viewClient = WebClient.builder().exchangeFunction(request -> {
            viewRequests.incrementAndGet();
            assertThat(request.url().getPath()).isEqualTo("/post-view/counts");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("{\"counts\":{\"1\":10,\"2\":5,\"3\":0}}")
                    .build());
        }).build();
        WebClient unused = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new AssertionError("Unexpected request"))).build();
        PostController controller = new PostController(
                jsonClient(new AtomicReference<>(), postsJson), unused, unused, unused, viewClient, jsonClient(new AtomicReference<>(), "[]"));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.index(model, new PostSearch(), "cookie-jwt");

        assertThat(viewRequests).hasValue(1);
        assertThat((List<PostListViewResponse>) model.get("posts"))
                .extracting(PostListViewResponse::getViewCount)
                .containsExactly(10L, 5L, 0L);
    }

    @Test
    void detailWithNoCommentsRendersEmptyCommentState() {
        PostController controller = new PostController(
                jsonClient(new AtomicReference<>(),
                        "{\"id\":1,\"title\":\"empty comments\",\"content\":\"body\","
                                + "\"writer\":\"writer\",\"createdDate\":\"2026.08.21\","
                                + "\"modifiedDate\":\"2026.08.21\"}"),
                jsonClient(new AtomicReference<>(),
                        "{\"id\":2,\"username\":\"user\",\"nickname\":\"writer\"}"),
                jsonClient(new AtomicReference<>(), "[]"),
                jsonClient(new AtomicReference<>(), "0"),
                jsonClient(new AtomicReference<>(), "0"),
                jsonClient(new AtomicReference<>(), "[]"));
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.getPost(1L, model, "cookie-jwt")).isEqualTo("posts/posts-read");
        assertThat(model.get("comments")).isEqualTo(List.of());
        assertThat(renderTemplate("templates/posts/posts-read.mustache", model))
                .contains("empty comments");
    }

    @Test
    void indexPlacesPostResponseListInModelAndRendersItsFields() {
        String json = """
                [{"id":1,"title":"실제 제목","writer":"작성자",
                  "createdDate":"2026.08.21"}]
                """;
        ExtendedModelMap model = invokeIndex(json);

        Object postsAttribute = model.get("posts");
        assertThat(postsAttribute).isInstanceOf(List.class);
        List<?> posts = (List<?>) postsAttribute;
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0)).isInstanceOf(PostListViewResponse.class);
        PostListViewResponse post = (PostListViewResponse) posts.get(0);
        assertThat(post.getCreatedDate()).isEqualTo("2026.08.21");
        assertThat(post.getViewCount()).isEqualTo(7L);

        assertThat(render(model))
                .contains("실제 제목", "작성자", "2026.08.21", "조회 7")
                .doesNotContain("아직 게시글이 없습니다");
    }

    @Test
    void indexRendersEmptyStateForEmptyPostServiceArray() {
        ExtendedModelMap model = invokeIndex("[]");

        assertThat(model.get("posts")).isEqualTo(List.of());
        assertThat(render(model)).contains("아직 게시글이 없습니다", "아직 집계된 인기 글이 없습니다.");
    }

    @Test
    void detailRelaysBearerToEveryServiceAndRendersSuccessfully() {
        AtomicReference<ClientRequest> postRequest = new AtomicReference<>();
        AtomicReference<ClientRequest> commentRequest = new AtomicReference<>();
        AtomicReference<ClientRequest> likeRequest = new AtomicReference<>();
        AtomicReference<ClientRequest> viewRequest = new AtomicReference<>();
        WebClient postClient = jsonClient(postRequest,
                "{\"id\":1,\"title\":\"상세 제목\",\"content\":\"본문\",\"writer\":\"작성자\","
                        + "\"createdDate\":\"2026.08.21\",\"modifiedDate\":\"2026.08.21\"}");
        PostController controller = new PostController(
                postClient,
                jsonClient(new AtomicReference<>(),
                        "{\"id\":2,\"username\":\"user\",\"nickname\":\"작성자\"}"),
                jsonClient(commentRequest, "[{\"id\":8,\"postId\":1,\"userId\":2,"
                        + "\"nickname\":\"작성자\",\"comments\":\"댓글 본문\","
                        + "\"createdDate\":\"2026.08.21\"}]"),
                jsonClient(likeRequest, "0"),
                jsonClient(viewRequest, "0"),
                jsonClient(new AtomicReference<>(), "[]"));
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.getPost(1L, model, "cookie-jwt")).isEqualTo("posts/posts-read");

        for (ClientRequest request : List.of(
                postRequest.get(), commentRequest.get(), likeRequest.get(), viewRequest.get())) {
            assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION))
                    .isEqualTo("Bearer cookie-jwt");
        }
        assertThat(renderTemplate("templates/posts/posts-read.mustache", model))
                .contains("상세 제목", "본문", "작성자", "2026.08.21", "댓글 본문");
    }

    @Test
    void missingPostProducesHttp404InsteadOfForwardingToMissingView() {
        WebClient notFoundPostClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()))
                .build();
        WebClient emptyClient = jsonClient(new AtomicReference<>(), "0");
        PostController controller = new PostController(
                notFoundPostClient, emptyClient, emptyClient, emptyClient, emptyClient, emptyClient);

        assertThatThrownBy(() -> controller.getPost(999L, new ExtendedModelMap(), null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void indexRendersRankedHotPostsFromTodaysApi() {
        AtomicReference<ClientRequest> hotRequest = new AtomicReference<>();
        WebClient hotClient = jsonClient(hotRequest,
                "[{\"postId\":42,\"title\":\"인기 제목\",\"createAt\":\"2026-10-16T12:00:00\"}]");
        WebClient unused = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new AssertionError("Unexpected request"))).build();
        PostController controller = new PostController(
                jsonClient(new AtomicReference<>(), "[]"), unused, unused, unused, unused, hotClient);
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.index(model, new PostSearch(), null)).isEqualTo("index");
        assertThat(hotRequest.get().url().getPath()).matches("/hot-posts/date/\\d{8}");
        assertThat(render(model)).contains("지금 주목받는 글", "인기 제목", "/posts/getPost/42")
                .doesNotContain("아직 집계된 인기 글이 없습니다");
    }

    @Test
    void hotPostFailureKeepsRecentPostsVisible() {
        WebClient unavailableHotClient = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new IllegalStateException("hot-post unavailable"))).build();
        WebClient unused = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new AssertionError("Unexpected request"))).build();
        PostController controller = new PostController(
                jsonClient(new AtomicReference<>(),
                        "[{\"id\":1,\"title\":\"최근 글\",\"writer\":\"작성자\",\"createdDate\":\"2026.10.16\"}]"),
                unused, unused, unused,
                jsonClient(new AtomicReference<>(), "{\"counts\":{\"1\":2}}"), unavailableHotClient);
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.index(model, new PostSearch(), null)).isEqualTo("index");
        assertThat(model.get("listLoadError")).isNull();
        assertThat(render(model)).contains("최근 글", "인기 글을 잠시 불러올 수 없습니다.");
    }

    @Test
    void loggedInUsersSeeTheirActualInitialLikeState() {
        for (boolean liked : List.of(true, false)) {
            AtomicReference<ClientRequest> stateRequest = new AtomicReference<>();
            WebClient likeClient = WebClient.builder().exchangeFunction(request -> {
                if (request.url().getPath().contains("/exists/")) {
                    stateRequest.set(request);
                }
                return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body(request.url().getPath().contains("/exists/") ? String.valueOf(liked) : "1")
                        .build());
            }).build();
            PostController controller = new PostController(
                    jsonClient(new AtomicReference<>(),
                            "{\"id\":42,\"title\":\"글\",\"content\":\"본문\",\"writer\":\"작성자\",\"createdDate\":\"2026.10.16\"}"),
                    jsonClient(new AtomicReference<>(),
                            "{\"id\":7,\"username\":\"user\",\"nickname\":\"작성자\"}"),
                    jsonClient(new AtomicReference<>(), "[]"), likeClient,
                    jsonClient(new AtomicReference<>(), "0"),
                    jsonClient(new AtomicReference<>(), "[]"));
            ExtendedModelMap model = new ExtendedModelMap();

            assertThat(controller.getPost(42L, model, "cookie-jwt")).isEqualTo("posts/posts-read");
            assertThat(stateRequest.get().url().getQuery()).isEqualTo("userId=7");
            assertThat(stateRequest.get().headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer cookie-jwt");
            assertThat(renderTemplate("templates/posts/posts-read.mustache", model))
                    .contains("aria-pressed=\"" + liked + "\"");
        }
    }

    @Test
    void likeStateFailureDoesNotHidePostDetails() {
        WebClient likeClient = WebClient.builder().exchangeFunction(request -> {
            if (request.url().getPath().contains("/exists/")) {
                return Mono.error(new IllegalStateException("like state unavailable"));
            }
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json").body("1").build());
        }).build();
        PostController controller = new PostController(
                jsonClient(new AtomicReference<>(),
                        "{\"id\":42,\"title\":\"본문 제목\",\"content\":\"본문\",\"writer\":\"작성자\",\"createdDate\":\"2026.10.16\"}"),
                jsonClient(new AtomicReference<>(),
                        "{\"id\":7,\"username\":\"user\",\"nickname\":\"작성자\"}"),
                jsonClient(new AtomicReference<>(), "[]"), likeClient,
                jsonClient(new AtomicReference<>(), "0"),
                jsonClient(new AtomicReference<>(), "[]"));
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.getPost(42L, model, "cookie-jwt")).isEqualTo("posts/posts-read");
        assertThat(renderTemplate("templates/posts/posts-read.mustache", model))
                .contains("본문 제목", "좋아요 상태를 확인할 수 없습니다.");
    }

    private ExtendedModelMap invokeIndex(String postServiceJson) {
        WebClient postClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body(postServiceJson)
                        .build()))
                .build();
        WebClient unusedClient = WebClient.builder()
                .exchangeFunction(request -> Mono.error(new AssertionError("Unexpected downstream request")))
                .build();
        WebClient viewClient = jsonClient(new AtomicReference<>(), "{\"counts\":{\"1\":7}}");
        PostController controller = new PostController(
                postClient, unusedClient, unusedClient, unusedClient, viewClient, jsonClient(new AtomicReference<>(), "[]"));
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.index(model, new PostSearch(), null)).isEqualTo("index");
        return model;
    }

    private String render(ExtendedModelMap model) {
        return renderTemplate("templates/index.mustache", model);
    }

    private String renderTemplate(String template, ExtendedModelMap model) {
        Mustache.Compiler compiler = Mustache.compiler().withLoader(name -> {
            var input = getClass().getClassLoader()
                    .getResourceAsStream("templates/" + name + ".mustache");
            if (input == null) {
                throw new IOException("Missing partial: " + name);
            }
            return new InputStreamReader(input, StandardCharsets.UTF_8);
        });
        var context = new HashMap<>(model.asMap());
        return compiler.compile(resource(template)).execute(context);
    }

    private WebClient jsonClient(AtomicReference<ClientRequest> captured, String body) {
        return WebClient.builder()
                .exchangeFunction(request -> {
                    captured.set(request);
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .body(body)
                            .build());
                })
                .build();
    }

    private String resource(String path) {
        try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Missing resource: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
