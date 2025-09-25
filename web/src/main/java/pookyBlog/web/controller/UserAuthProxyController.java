package pookyBlog.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.LoginDto;
import pookyBlog.common.Dto.Request.SignUpDto;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthProxyController {
    private final WebClient userWebClient;

    @PostMapping("/signup")
    public Mono<ResponseEntity<Map>> signUp(@RequestBody SignUpDto signUpDto) {
        return userWebClient.post()
                .uri("/api/auth/signup")
                .bodyValue(signUpDto)
                .retrieve()
                .toEntity(Map.class);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map>> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        return userWebClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginDto)
                .retrieve()
                .toEntity(Map.class) // user-service가 { "accessToken": "...", "refreshToken": "..." } 등을 반환한다고 가정
                .doOnSuccess(entity -> {
                    if (entity.getBody() != null && entity.getBody().containsKey("accessToken")) {
                        String accessToken = (String) entity.getBody().get("accessToken");
                        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", accessToken)
                                .httpOnly(true)
                                .secure(true) // HTTPS 환경에서만
                                .sameSite("Strict")
                                .path("/")
                                .maxAge(3600) // 1시간
                                .build();
                        response.addHeader("Set-Cookie", jwtCookie.toString());
                    }
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map>> logout(HttpServletResponse response) {
        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", jwtCookie.toString());

        // 내부 user-service의 로그아웃 로직도 호출 (필요 시)
        return userWebClient.post()
                .uri("/api/auth/logout")
                .retrieve()
                .toEntity(Map.class);
    }
}
