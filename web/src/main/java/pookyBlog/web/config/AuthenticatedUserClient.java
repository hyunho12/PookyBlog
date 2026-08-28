package pookyBlog.web.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Response.AuthMeResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserClient {
    private final WebClient userWebClient;

    public Mono<AuthMeResponse> currentUser() {
        return userWebClient.get()
                .uri("/auth/me")
                .retrieve()
                .bodyToMono(AuthMeResponse.class);
    }

    public Mono<AuthMeResponse> currentUser(String jwtToken) {
        return userWebClient.get()
                .uri("/auth/me")
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .retrieve()
                .bodyToMono(AuthMeResponse.class);
    }
}
