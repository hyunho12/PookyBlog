package pookyBlog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.CommentCreate;
import pookyBlog.common.Dto.Request.CommentUpdate;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentProxyController {
    private final WebClient commentWebClient;

    @PostMapping
    public Mono<ResponseEntity<Long>> createComment(@RequestBody CommentCreate request) {
        return commentWebClient.post()
                .uri("/api/comments")
                .bodyValue(request)
                .retrieve()
                .toEntity(Long.class);
    }

    @PutMapping("/{commentId}")
    public Mono<ResponseEntity<Void>> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdate request) {
        return commentWebClient.put()
                .uri("/api/comments/{commentId}", commentId)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity();
    }

    @DeleteMapping("/{commentId}")
    public Mono<ResponseEntity<Void>> deleteComment(@PathVariable Long commentId) {
        return commentWebClient.delete()
                .uri("/api/comments/{commentId}", commentId)
                .retrieve()
                .toBodilessEntity();
    }
}
