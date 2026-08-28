package pookyBlog.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;

class DownstreamExceptionHandlerTest {

    @Test
    void downstreamForbiddenRemainsForbidden() {
        WebClientResponseException exception = WebClientResponseException.create(
                403, "Forbidden", null, null, null);

        var response = new DownstreamExceptionHandler().handleDownstreamError(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("403");
        assertThat(response.getBody().getMessage()).contains("권한");
    }

    @Test
    void downstreamClientErrorMessageIsPreserved() {
        byte[] body = "{\"code\":\"INVALID_LOGIN\",\"message\":\"아이디 또는 비밀번호가 올바르지 않습니다.\"}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        WebClientResponseException exception = WebClientResponseException.create(
                401, "Unauthorized", null, body, java.nio.charset.StandardCharsets.UTF_8);

        var response = new DownstreamExceptionHandler().handleDownstreamError(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_LOGIN");
        assertThat(response.getBody().getMessage()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}
