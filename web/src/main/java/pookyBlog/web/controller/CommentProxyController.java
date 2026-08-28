package pookyBlog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.CommentCreate;
import pookyBlog.common.Dto.Request.CommentUpdate;
import reactor.core.publisher.Mono;
import pookyBlog.web.config.AuthenticatedUserClient;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentProxyController {
    private final WebClient commentWebClient;
    private final AuthenticatedUserClient authenticatedUserClient;

    @GetMapping("/posts/{postId}/comments")
    public Mono<ResponseEntity<java.util.List<pookyBlog.common.Dto.Response.CommentResponse>>> getComments(@PathVariable Long postId) {
        return commentWebClient.get()
                .uri("/api/posts/{postId}", postId)
                .retrieve()
                .toEntityList(pookyBlog.common.Dto.Response.CommentResponse.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()));
    }

    @PostMapping("/posts/{postId}/comments")
    public Mono<ResponseEntity<Long>> createComment(@CookieValue(name = "jwtToken") String jwtToken,
                                                     @PathVariable Long postId, @RequestBody CommentCreate request) {
        return authenticatedUserClient.currentUser(jwtToken).flatMap(user -> {
        request.setPostsId(postId);
        request.setUserId(user.id());
        return commentWebClient.post()
                .uri("/api/comments/create")
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .bodyValue(request)
                .retrieve()
                .toEntity(Long.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()));
        });
    }

    @PutMapping("/comments/{commentId}")
    public Mono<ResponseEntity<Void>> updateComment(@CookieValue(name = "jwtToken") String jwtToken,
                                                     @PathVariable Long commentId, @RequestBody CommentUpdate request) {
        return authenticatedUserClient.currentUser(jwtToken).flatMap(user -> commentWebClient.put()
                .uri(uriBuilder -> uriBuilder.path("/api/comments/{commentId}")
                        .queryParam("userId", user.id()).build(commentId))
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .map(response -> ResponseEntity.status(response.getStatusCode()).build()));
    }

    @DeleteMapping("/comments/{commentId}")
    public Mono<ResponseEntity<Void>> deleteComment(@CookieValue(name = "jwtToken") String jwtToken,
                                                     @PathVariable Long commentId) {
        return authenticatedUserClient.currentUser(jwtToken).flatMap(user -> commentWebClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/comments/{commentId}")
                        .queryParam("userId", user.id()).build(commentId))
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .retrieve()
                .toBodilessEntity()
                .map(response -> ResponseEntity.status(response.getStatusCode()).build()));
    }
}
