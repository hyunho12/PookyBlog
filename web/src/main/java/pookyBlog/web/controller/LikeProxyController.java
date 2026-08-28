package pookyBlog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import pookyBlog.web.config.AuthenticatedUserClient;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeProxyController {
    private final WebClient likeWebClient;
    private final AuthenticatedUserClient authenticatedUserClient;

    // 예시: 로그인된 사용자 ID를 SecurityContext 등에서 가져온다고 가정
    // Long userId = ... ;
    @PostMapping
    public Mono<ResponseEntity<String>> likePost(@CookieValue(name = "jwtToken") String jwtToken,
                                                  @PathVariable Long postId) {
        return authenticatedUserClient.currentUser(jwtToken).flatMap(user -> likeWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/likes/{postId}")
                        .queryParam("userId", user.id())
                        .build(postId))
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .retrieve()
                .toEntity(String.class));
    }

    @DeleteMapping
    public Mono<ResponseEntity<String>> unlikePost(@CookieValue(name = "jwtToken") String jwtToken,
                                                    @PathVariable Long postId) {
        return authenticatedUserClient.currentUser(jwtToken).flatMap(user -> likeWebClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/likes/{postId}")
                        .queryParam("userId", user.id())
                        .build(postId))
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .retrieve()
                .toEntity(String.class));
    }

    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countLikes(@PathVariable Long postId) {
        return likeWebClient.get()
                .uri("/likes/count/{postId}", postId)
                .retrieve()
                .toEntity(Long.class);
    }
}
