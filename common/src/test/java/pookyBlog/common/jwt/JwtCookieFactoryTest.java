package pookyBlog.common.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtCookieFactoryTest {
    @Test
    void localCookieIsHttpOnlyAndNotSecure() {
        String cookie = new JwtCookieFactory(false, "Strict", 3600).create("token").toString();

        assertThat(cookie).contains("jwtToken=token", "HttpOnly", "SameSite=Strict");
        assertThat(cookie).doesNotContain("Secure");
    }

    @Test
    void productionCookieIsSecureAndLogoutExpiresIt() {
        JwtCookieFactory factory = new JwtCookieFactory(true, "Strict", 3600);

        assertThat(factory.create("token").toString()).contains("Secure", "HttpOnly");
        assertThat(factory.expire().toString()).contains("jwtToken=", "Max-Age=0", "HttpOnly", "Secure");
    }
}
