package pookyBlog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.PostCreate;
import pookyBlog.common.Dto.Request.PostUpdate;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostProxyController {
    private final WebClient postWebClient;

    @PostMapping
    public Mono<ResponseEntity<Void>> createPost(@RequestBody PostCreate request) {
        return postWebClient.post()
                .uri("/api/posts")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity();
    }

    @PatchMapping("/{postId}")
    public Mono<ResponseEntity<Void>> updatePost(@PathVariable Long postId, @RequestBody PostUpdate request) {
        return postWebClient.patch()
                .uri("/api/posts/{postId}", postId)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity();
    }

    @DeleteMapping("/{postId}")
    public Mono<ResponseEntity<Void>> deletePost(@PathVariable Long postId) {
        return postWebClient.delete()
                .uri("/api/posts/{postId}", postId)
                .retrieve()
                .toBodilessEntity();
    }
}
