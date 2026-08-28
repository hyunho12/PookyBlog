package pookyBlog.post.controller;

import pookyBlog.common.Dto.Request.PostCreate;
import pookyBlog.common.Dto.Request.PostSearch;
import pookyBlog.common.Dto.Request.PostUpdate;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.common.Dto.Response.PostListResponse;
import pookyBlog.post.Service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Rest API Controller

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

    @PostMapping("/posts/create")
    public void createPost(@RequestBody @Valid PostCreate request) {
        postService.write(request);
    }

    @GetMapping("/posts/{postId}")
    public PostResponse getPost(@PathVariable Long postId){
        return postService.get(postId);
    }

    @GetMapping("/posts")
    public List<PostListResponse> getListPosts(@ModelAttribute PostSearch postSearch){
        return postService.getListPosts(postSearch);
    }

    @PatchMapping("/posts/update/{postId}")
    public void updatePost(@PathVariable Long postId, @RequestBody @Valid PostUpdate request){
        postService.update(postId, request);
    }

    @DeleteMapping("/posts/delete/{postId}")
    public void delete(@PathVariable("postId") Long postId){
        postService.delete(postId);
    }

    @GetMapping("/posts/count/{postId}")
    public Long count(@PathVariable Long postId){
        return postService.count(postId);
    }
}
