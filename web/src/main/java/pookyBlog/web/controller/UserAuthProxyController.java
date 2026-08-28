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
                .exchangeToMono(upstream ->
                        upstream.bodyToMono(Map.class)
                                .map(body -> {
                                    ResponseEntity.BodyBuilder builder =
                                            ResponseEntity.status(upstream.statusCode());

                                    String setCookie = upstream.headers()
                                            .asHttpHeaders()
                                            .getFirst(HttpHeaders.SET_COOKIE);

                                    if (setCookie != null) {
                                        builder.header(HttpHeaders.SET_COOKIE, setCookie);
                                    }

                                    return builder.body(body);
                                })
                );
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<AuthMeResponse>> me() {
        return userWebClient.get().uri("/auth/me").retrieve().toEntity(AuthMeResponse.class);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map>> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieFactory.expire().toString());
        return userWebClient.post().uri("/auth/logout").retrieve().toEntity(Map.class);
    }
}
