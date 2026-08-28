package pookyBlog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import pookyBlog.web.config.AuthenticatedUserClient;

@RestController
@RequestMapping("/api/posts/{postId}/views")
@RequiredArgsConstructor
public class ViewProxyController {
    private final WebClient viewWebClient;
    private final AuthenticatedUserClient authenticatedUserClient;

    @PostMapping
    public Mono<ResponseEntity<Long>> increaseView(@CookieValue(name = "jwtToken") String jwtToken,
                                                    @PathVariable Long postId) {
        return authenticatedUserClient.currentUser(jwtToken).flatMap(user -> viewWebClient.post()
                .uri("/post-view/{postId}/users/{userId}", postId, user.id())
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .retrieve().toEntity(Long.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody())));
    }

    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countViews(@PathVariable Long postId) {
        return viewWebClient.get()
                .uri("/post-view/{postId}/count", postId)
                .retrieve()
                .toEntity(Long.class)
                .map(response -> ResponseEntity.status(response.getStatusCode()).body(response.getBody()));
    }
}
