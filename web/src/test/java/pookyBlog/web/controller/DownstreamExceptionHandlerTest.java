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
}
