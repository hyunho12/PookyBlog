package pookyBlog.common.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JwtCookieFactory {
    private final boolean secure;
    private final String sameSite;
    private final Duration maxAge;

    public JwtCookieFactory(@Value("${auth.cookie.secure:false}") boolean secure,
                            @Value("${auth.cookie.same-site:Strict}") String sameSite,
                            @Value("${auth.cookie.max-age:3600}") long maxAgeSeconds) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAge = Duration.ofSeconds(maxAgeSeconds);
    }

    public ResponseCookie create(String token) {
        return base(token).maxAge(maxAge).build();
    }

    public ResponseCookie expire() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from("jwtToken", value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/");
    }
}
