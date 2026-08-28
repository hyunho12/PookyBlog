package pookyBlog.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.CommentCreate;
import pookyBlog.common.Dto.Request.CommentUpdate;
import pookyBlog.common.Dto.Response.AuthMeResponse;
import pookyBlog.web.config.AuthenticatedUserClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProxyMutationJwtRelayTest {
    private AuthenticatedUserClient authenticatedUserClient;

    @BeforeEach
    void authenticatedUser() {
        authenticatedUserClient = mock(AuthenticatedUserClient.class);
        when(authenticatedUserClient.currentUser("cookie-jwt"))
                .thenReturn(Mono.just(new AuthMeResponse(7L, "user", "writer")));
    }

    @Test
    void commentCreateUpdateAndDeleteRelayCapturedBearer() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        CommentProxyController controller = new CommentProxyController(
                client(captured, "1"), authenticatedUserClient);
        CommentCreate create = new CommentCreate();
        create.setComment("comment");

        controller.createComment("cookie-jwt", 3L, create).block();
        assertBearer(captured.get());
        assertThat(create.getUserId()).isEqualTo(7L);
        assertThat(create.getPostsId()).isEqualTo(3L);

        controller.updateComment("cookie-jwt", 5L, new CommentUpdate()).block();
        assertBearer(captured.get());
        assertThat(captured.get().url().getPath()).isEqualTo("/api/comments/5");
        assertThat(captured.get().url().getQuery()).isEqualTo("userId=7");

        controller.deleteComment("cookie-jwt", 5L).block();
        assertBearer(captured.get());
        assertThat(captured.get().url().getPath()).isEqualTo("/api/comments/5");
        assertThat(captured.get().url().getQuery()).isEqualTo("userId=7");
    }

    @Test
    void likeAndUnlikeRelayCapturedBearer() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        LikeProxyController controller = new LikeProxyController(
                client(captured, "\"ok\""), authenticatedUserClient);

        controller.likePost("cookie-jwt", 3L).block();
        assertBearer(captured.get());
        controller.unlikePost("cookie-jwt", 3L).block();
        assertBearer(captured.get());
    }

    @Test
    void viewMutationRelaysCapturedBearer() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ViewProxyController controller = new ViewProxyController(
                client(captured, "1"), authenticatedUserClient);

        controller.increaseView("cookie-jwt", 3L).block();

        assertBearer(captured.get());
    }

    @Test
    void countEndpointsExposePlainJsonNumbersExpectedByJavascript() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        LikeProxyController likeController = new LikeProxyController(
                client(captured, "4"), authenticatedUserClient);
        ViewProxyController viewController = new ViewProxyController(
                client(captured, "9"), authenticatedUserClient);

        assertThat(likeController.countLikes(3L).block().getBody()).isEqualTo(4L);
        assertThat(viewController.countViews(3L).block().getBody()).isEqualTo(9L);
    }

    private WebClient client(AtomicReference<ClientRequest> captured, String body) {
        return WebClient.builder().exchangeFunction(request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(body).build());
        }).build();
    }

    private void assertBearer(ClientRequest request) {
        assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer cookie-jwt");
    }
}
