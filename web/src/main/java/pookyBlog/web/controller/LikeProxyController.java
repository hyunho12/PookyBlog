package pookyBlog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeProxyController {
    private final WebClient likeWebClient;

    // 예시: 로그인된 사용자 ID를 SecurityContext 등에서 가져온다고 가정
    // Long userId = ... ;
    @PostMapping
    public Mono<ResponseEntity<String>> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        return likeWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/posts/{postId}/likes")
                        .queryParam("userId", userId)
                        .build(postId))
                .retrieve()
                .toEntity(String.class);
    }

    @DeleteMapping
    public Mono<ResponseEntity<String>> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        return likeWebClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/posts/{postId}/likes")
                        .queryParam("userId", userId)
                        .build(postId))
                .retrieve()
                .toEntity(String.class);
    }
}
