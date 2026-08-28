package pookyBlog.web.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.Arrays;

@Component
public class JwtRelayExchangeFilter {
    public ExchangeFilterFunction filter() {
        return (request, next) -> {
            String token = currentJwtCookie();
            if (token == null || request.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
                return next.exchange(request);
            }
            ClientRequest authenticated = ClientRequest.from(request)
                    .headers(headers -> headers.setBearerAuth(token))
                    .build();
            return next.exchange(authenticated);
        };
    }

    private String currentJwtCookie() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> "jwtToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }
}
