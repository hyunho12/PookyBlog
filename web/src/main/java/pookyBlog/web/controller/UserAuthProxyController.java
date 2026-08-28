package pookyBlog.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.LoginDto;
import pookyBlog.common.Dto.Request.SignUpDto;
import pookyBlog.common.Dto.Response.AuthMeResponse;
import pookyBlog.common.jwt.JwtCookieFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthProxyController {
    private final WebClient userWebClient;
    private final JwtCookieFactory jwtCookieFactory;

    @PostMapping("/signup")
    public Mono<ResponseEntity<Map>> signUp(@RequestBody SignUpDto signUpDto) {
        return userWebClient.post()
                .uri("/auth/signup")
                .bodyValue(signUpDto)
                .retrieve()
                .bodyToMono(Map.class)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map>> login(@RequestBody LoginDto loginDto) {
        return userWebClient.post()
                .uri("/auth/login")
                .bodyValue(loginDto)
                .exchangeToMono(upstream -> {
                    if (upstream.statusCode().isError()) {
                        return upstream.createException().flatMap(Mono::error);
                    }
                    return upstream.bodyToMono(Map.class)
                            .defaultIfEmpty(Map.of())
                            .map(body -> {
                                ResponseEntity.BodyBuilder builder =
                                        ResponseEntity.status(upstream.statusCode());

                                upstream.headers()
                                        .header(HttpHeaders.SET_COOKIE)
                                        .forEach(cookie -> builder.header(HttpHeaders.SET_COOKIE, cookie));

                                return builder.body(body);
                            });
                });
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<AuthMeResponse>> me() {
        return userWebClient.get()
                .uri("/auth/me")
                .retrieve()
                .bodyToMono(AuthMeResponse.class)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map>> logout(HttpServletResponse response) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                jwtCookieFactory.expire().toString()
        );

        return userWebClient.post()
                .uri("/auth/logout")
                .retrieve()
                .bodyToMono(Map.class)
                .map(ResponseEntity::ok);
    }
}
