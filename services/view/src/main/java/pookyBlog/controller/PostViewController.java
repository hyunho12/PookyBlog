package pookyBlog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pookyBlog.Service.PostViewService;

@RequestMapping("/post-view")
@RestController
@RequiredArgsConstructor
public class PostViewController {
    private PostViewService postViewService;

    @PostMapping("/{postId}/users/{userId}")
    public Long increase(@PathVariable("postId") Long postId, @PathVariable("userId") Long userId){
        return postViewService.increase(postId, userId);
    }

    @GetMapping("/{postId}/count")
    public Long count(@PathVariable("postId") Long postId){
        return postViewService.count(postId);
    }
}
