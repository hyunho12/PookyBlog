package com.pookyBlog.pookyBlog.controller;

import com.pookyBlog.pookyBlog.Dto.Request.PostCreate;
import com.pookyBlog.pookyBlog.Dto.Request.PostSearch;
import com.pookyBlog.pookyBlog.Dto.Request.PostUpdate;
import com.pookyBlog.pookyBlog.Dto.Response.PostResponse;
import com.pookyBlog.pookyBlog.Service.PostService;
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
    public List<PostResponse> getListPosts(@ModelAttribute PostSearch postSearch){
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
}
