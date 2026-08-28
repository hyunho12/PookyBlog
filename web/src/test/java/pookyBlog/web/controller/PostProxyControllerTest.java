package pookyBlog.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.PostCreate;
import pookyBlog.common.Dto.Request.PostUpdate;
import pookyBlog.common.Dto.Request.PostSearch;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.common.Dto.Response.AuthMeResponse;
import pookyBlog.web.config.AuthenticatedUserClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostProxyControllerTest {

    @Test
    void listCombinesThreeCountsUsingOneViewBatchRequest() {
        java.util.concurrent.atomic.AtomicInteger batchRequests = new java.util.concurrent.atomic.AtomicInteger();
        WebClient postClient = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body("[{\"id\":1,\"title\":\"one\",\"writer\":\"a\",\"createdDate\":\"d\"},"
                                + "{\"id\":2,\"title\":\"two\",\"writer\":\"b\",\"createdDate\":\"d\"},"
                                + "{\"id\":3,\"title\":\"three\",\"writer\":\"c\",\"createdDate\":\"d\"}]")
                        .build())).build();
        WebClient viewClient = WebClient.builder().exchangeFunction(request -> {
            batchRequests.incrementAndGet();
            assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION))
                    .isEqualTo("Bearer cookie-jwt");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("{\"counts\":{\"1\":10,\"2\":5,\"3\":0}}")
                    .build());
        }).build();
        PostProxyController controller = new PostProxyController(
                postClient, viewClient, mock(AuthenticatedUserClient.class));

        var response = controller.getPosts(new PostSearch(), "cookie-jwt").block();

        assertThat(batchRequests).hasValue(1);
        assertThat(response.getBody()).extracting(item -> item.getViewCount())
                .containsExactly(10L, 5L, 0L);
    }

    @Test
    void createRelaysCapturedCookieJwtAfterAsyncAuthenticationLookup() {
        AtomicReference<ClientRequest> downstreamRequest = new AtomicReference<>();
        WebClient postWebClient = WebClient.builder()
                .exchangeFunction(request -> {
                    downstreamRequest.set(request);
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                })
                .build();
        AuthenticatedUserClient authenticatedUserClient = mock(AuthenticatedUserClient.class);
        when(authenticatedUserClient.currentUser("cookie-jwt"))
                .thenReturn(Mono.just(new AuthMeResponse(1L, "user", "writer")));
        PostCreate post = PostCreate.builder()
                .title("title")
                .content("content")
                .build();

        new PostProxyController(postWebClient, postWebClient, authenticatedUserClient)
                .createPost("cookie-jwt", post)
                .block();

        assertThat(downstreamRequest.get().headers().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer cookie-jwt");
        assertThat(post.getWriter()).isEqualTo("writer");
    }

    @Test
    void updateAndDeleteExecuteMutationAfterOwnerCheck() {
        java.util.List<ClientRequest> requests = new java.util.ArrayList<>();
        WebClient postWebClient = WebClient.builder()
                .exchangeFunction(request -> {
                    requests.add(request);
                    if (request.method() == org.springframework.http.HttpMethod.GET) {
                        return Mono.just(ClientResponse.create(HttpStatus.OK)
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .body("{\"id\":1,\"title\":\"old\",\"content\":\"old\","
                                        + "\"writer\":\"writer\",\"createdDate\":\"2026.08.21\","
                                        + "\"view\":0,\"comments\":[]}")
                                .build());
                    }
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                }).build();
        AuthenticatedUserClient userClient = mock(AuthenticatedUserClient.class);
        when(userClient.currentUser("cookie-jwt"))
                .thenReturn(Mono.just(new AuthMeResponse(1L, "user", "writer")));
        PostProxyController controller = new PostProxyController(postWebClient, postWebClient, userClient);

        controller.updatePost("cookie-jwt", 1L, PostUpdate.builder()
                .title("new").content("new content").build()).block();
        assertThat(requests).anyMatch(request -> request.method() == org.springframework.http.HttpMethod.PATCH
                && request.url().getPath().equals("/posts/update/1"));

        requests.clear();
        controller.deletePost("cookie-jwt", 1L).block();
        assertThat(requests).anyMatch(request -> request.method() == org.springframework.http.HttpMethod.DELETE
                && request.url().getPath().equals("/posts/delete/1"));
    }

    @Test
    void getPostKeepsStatusAndBodyWithoutDownstreamHeaders() {
        WebClient postWebClient = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.ACCEPTED)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                        .header(HttpHeaders.CONNECTION, "keep-alive")
                        .body("{\"id\":1,\"title\":\"title\",\"content\":\"content\"," +
                                "\"writer\":\"writer\",\"createdDate\":\"2026.08.29\"," +
                                "\"view\":0,\"comments\":[]}")
                        .build())).build();
        PostProxyController controller = new PostProxyController(
                postWebClient, postWebClient, mock(AuthenticatedUserClient.class));

        var response = controller.getPost(1L).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("title");
        assertThat(response.getHeaders()).doesNotContainKeys(
                HttpHeaders.TRANSFER_ENCODING, HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONNECTION);
    }
}
