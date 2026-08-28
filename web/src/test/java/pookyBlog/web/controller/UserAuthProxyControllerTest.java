package pookyBlog.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pookyBlog.common.Dto.Request.LoginDto;
import pookyBlog.common.jwt.JwtCookieFactory;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UserAuthProxyControllerTest {

    @Test
    void loginForwardsOnlySetCookieHeader() {
        WebClient userClient = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header(HttpHeaders.SET_COOKIE, "jwtToken=token; Path=/; HttpOnly")
                        .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
                        .header(HttpHeaders.CONNECTION, "keep-alive")
                        .body("{\"result\":\"ok\"}")
                        .build())).build();
        UserAuthProxyController controller = new UserAuthProxyController(
                userClient, mock(JwtCookieFactory.class));

        var response = controller.login(new LoginDto()).block();

        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE))
                .containsExactly("jwtToken=token; Path=/; HttpOnly");
        assertThat(response.getHeaders()).doesNotContainKeys(
                HttpHeaders.TRANSFER_ENCODING, HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONNECTION);
        assertThat(response.getBody()).containsEntry("result", "ok");
    }

    @Test
    void loginErrorIsDelegatedToDownstreamExceptionHandler() {
        WebClient userClient = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.UNAUTHORIZED)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body("{\"code\":\"401\",\"message\":\"login failed\"}")
                        .build())).build();
        UserAuthProxyController controller = new UserAuthProxyController(
                userClient, mock(JwtCookieFactory.class));

        assertThatThrownBy(() -> controller.login(new LoginDto()).block())
                .isInstanceOf(WebClientResponseException.Unauthorized.class)
                .hasMessageContaining("401 Unauthorized");
    }
}