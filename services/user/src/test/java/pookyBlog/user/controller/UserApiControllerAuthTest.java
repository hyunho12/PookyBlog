package pookyBlog.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import pookyBlog.common.Dto.JwtToken;
import pookyBlog.common.Dto.Request.LoginDto;
import pookyBlog.common.jwt.JwtCookieFactory;
import pookyBlog.user.Security.auth.UserTokenProvider;
import pookyBlog.user.Service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserApiControllerAuthTest {
    private final UserService userService = mock(UserService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final UserTokenProvider tokenProvider = mock(UserTokenProvider.class);
    private final JwtCookieFactory cookieFactory = new JwtCookieFactory(false, "Strict", 3600);
    private final UserApiController controller =
            new UserApiController(userService, authenticationManager, tokenProvider, cookieFactory);

    @Test
    void successfulLoginSetsLocalHttpOnlyCookieWithoutExposingToken() {
        var authentication = new UsernamePasswordAuthenticationToken("tester", "", List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn(JwtToken.builder()
                .accessToken("signed-jwt")
                .build());
        LoginDto login = new LoginDto();
        login.setUsername("tester");
        login.setPassword("password");
        MockHttpServletResponse response = new MockHttpServletResponse();

        var result = controller.login(login, response);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeader("Set-Cookie"))
                .contains("jwtToken=signed-jwt", "HttpOnly", "SameSite=Strict")
                .doesNotContain("Secure");
        assertThat(result.getBody().toString()).doesNotContain("signed-jwt", "accessToken");
    }

    @Test
    void logoutExpiresCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.logout(response);

        assertThat(response.getHeader("Set-Cookie"))
                .contains("jwtToken=", "Max-Age=0", "HttpOnly");
    }
}
