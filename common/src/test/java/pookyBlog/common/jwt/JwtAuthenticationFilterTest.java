package pookyBlog.common.jwt;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {
    private final JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bearerTokenHasPriorityAndAuthenticates() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-token");
        request.setCookies(new Cookie("jwtToken", "cookie-token"));
        authenticate("header-token");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> { });

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("tester");
        verify(tokenProvider).validateToken("header-token");
        verify(tokenProvider, never()).validateToken("cookie-token");
    }

    @Test
    void cookieTokenIsFallbackWhenBearerIsAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwtToken", "cookie-token"));
        authenticate("cookie-token");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> { });

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("tester");
    }

    @Test
    void malformedAuthorizationFailsSafelyWithoutCookieFallback() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic invalid");
        request.setCookies(new Cookie("jwtToken", "cookie-token"));

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> { });

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken("cookie-token");
    }

    private void authenticate(String token) {
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getAuthentication(token))
                .thenReturn(new UsernamePasswordAuthenticationToken("tester", "", List.of()));
    }
}
