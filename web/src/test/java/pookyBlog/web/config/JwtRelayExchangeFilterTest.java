package pookyBlog.web.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRelayExchangeFilterTest {
    @AfterEach
    void resetRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void cookieJwtIsRelayedAsBearer() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(new Cookie("jwtToken", "cookie-jwt"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        AtomicReference<ClientRequest> captured = new AtomicReference<>();

        new JwtRelayExchangeFilter().filter()
                .filter(ClientRequest.create(org.springframework.http.HttpMethod.GET, java.net.URI.create("http://service/test")).build(),
                        request -> {
                            captured.set(request);
                            return reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.OK).build());
                        })
                .block();

        assertThat(captured.get().headers().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer cookie-jwt");
    }
}
