package pookyBlog.view.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pookyBlog.view.Service.PostViewService;
import pookyBlog.common.Dto.Request.PostViewCountsRequest;
import pookyBlog.common.Dto.Response.PostViewCountsResponse;

@RequestMapping("/post-view")
@RestController
@RequiredArgsConstructor
public class PostViewController {
    private final PostViewService postViewService;

    @PostMapping("/{postId}/users/{userId}")
    public Long increase(@PathVariable("postId") Long postId, @PathVariable("userId") Long userId){
        return postViewService.increase(postId, userId);
    }

    @GetMapping("/{postId}/count")
    public Long count(@PathVariable("postId") Long postId){
        return postViewService.count(postId);
    }

    @PostMapping("/counts")
    public PostViewCountsResponse counts(@RequestBody PostViewCountsRequest request) {
        return new PostViewCountsResponse(postViewService.counts(request.postIds()));
    }
}
