package pookyBlog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pookyBlog.response.PostReadResponse;
import pookyBlog.service.PostReadService;

@RestController
@RequiredArgsConstructor
public class PostReadApiController {
    private final PostReadService postReadService;

    @GetMapping("/posts/{postId}")
    public PostReadResponse read(@PathVariable("postId") Long postId){
        return postReadService.read(postId);
    }
}
