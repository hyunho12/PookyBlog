package pookyBlog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pookyBlog.common.Dto.Request.PostCreate;
import pookyBlog.common.Dto.Request.PostSearch;
import pookyBlog.common.Dto.Request.PostViewCountsRequest;
import pookyBlog.common.Dto.Request.PostUpdate;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.common.Dto.Response.PostListResponse;
import pookyBlog.common.Dto.Response.PostListViewResponse;
import pookyBlog.common.Dto.Response.PostViewCountsResponse;
import pookyBlog.web.config.AuthenticatedUserClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostProxyController {
    private final WebClient postWebClient;
    private final WebClient viewWebClient;
    private final AuthenticatedUserClient authenticatedUserClient;

    @GetMapping
    public Mono<ResponseEntity<List<PostListViewResponse>>> getPosts(
            @ModelAttribute PostSearch search,
            @CookieValue(name = "jwtToken", required = false) String jwtToken) {
        return postWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/posts")
                        .queryParam("page", search.getPage())
                        .queryParam("size", search.getSize())
                        .build())
                .retrieve()
                .bodyToFlux(PostListResponse.class)
                .collectList()
                .flatMap(posts -> {
                    if (posts.isEmpty()) {
                        return Mono.just(ResponseEntity.ok(List.of()));
                    }
                    return viewWebClient.post()
                            .uri("/post-view/counts")
                            .headers(headers -> {
                                if (jwtToken != null && !jwtToken.isBlank()) {
                                    headers.setBearerAuth(jwtToken);
                                }
                            })
                            .bodyValue(new PostViewCountsRequest(
                                    posts.stream().map(PostListResponse::getId).toList()))
                            .retrieve()
                            .bodyToMono(PostViewCountsResponse.class)
                            .map(counts -> ResponseEntity.ok(posts.stream()
                                    .map(post -> new PostListViewResponse(
                                            post, counts.counts().get(post.getId())))
                                    .toList()));
                });
    }

    @GetMapping("/{postId}")
    public Mono<ResponseEntity<PostResponse>> getPost(@PathVariable Long postId) {
        return postWebClient.get()
                .uri("/posts/{postId}", postId)
                .retrieve()
                .toEntity(PostResponse.class);
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> createPost(
            @CookieValue(name = "jwtToken") String jwtToken,
            @RequestBody PostCreate request) {
        return authenticatedUserClient.currentUser(jwtToken).flatMap(user -> {
            request.setWriter(user.nickname());
            return postWebClient.post().uri("/posts/create")
                    .headers(headers -> headers.setBearerAuth(jwtToken))
                    .bodyValue(request)
                    .retrieve().toBodilessEntity();
        });
    }

    @PatchMapping("/{postId}")
    public Mono<ResponseEntity<Void>> updatePost(
            @CookieValue(name = "jwtToken") String jwtToken,
            @PathVariable Long postId,
            @RequestBody PostUpdate request) {
        return requirePostOwner(postId, jwtToken).then(Mono.defer(() -> postWebClient.patch()
                .uri("/posts/update/{postId}", postId)
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .bodyValue(request)
                .retrieve().toBodilessEntity()));
    }

    @DeleteMapping("/{postId}")
    public Mono<ResponseEntity<Void>> deletePost(
            @CookieValue(name = "jwtToken") String jwtToken,
            @PathVariable Long postId) {
        return requirePostOwner(postId, jwtToken).then(Mono.defer(() -> postWebClient.delete()
                .uri("/posts/delete/{postId}", postId)
                .headers(headers -> headers.setBearerAuth(jwtToken))
                .retrieve().toBodilessEntity()));
    }

    private Mono<Void> requirePostOwner(Long postId, String jwtToken) {
        return Mono.zip(authenticatedUserClient.currentUser(jwtToken),
                        postWebClient.get().uri("/posts/{postId}", postId)
                                .headers(headers -> headers.setBearerAuth(jwtToken))
                                .retrieve().bodyToMono(PostResponse.class))
                .flatMap(tuple -> tuple.getT1().nickname().equals(tuple.getT2().getWriter())
                        ? Mono.empty()
                        : Mono.error(new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.FORBIDDEN, "작성자만 변경할 수 있습니다.")));
    }
}
