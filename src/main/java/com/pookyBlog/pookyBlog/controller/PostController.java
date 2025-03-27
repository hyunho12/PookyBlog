package com.pookyBlog.pookyBlog.controller;

import com.pookyBlog.pookyBlog.Dto.Request.PostSearch;
import com.pookyBlog.pookyBlog.Dto.Response.PostResponse;
import com.pookyBlog.pookyBlog.Entity.Comment;
import com.pookyBlog.pookyBlog.Entity.Post;
import com.pookyBlog.pookyBlog.Entity.User;
import com.pookyBlog.pookyBlog.Security.auth.CustomUserDetails;
import com.pookyBlog.pookyBlog.Service.PostService;
import com.pookyBlog.pookyBlog.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping("/")
    public String index(Model model, PostSearch postSearch){
        PageRequest pageRequest = PageRequest.of(postSearch.getPage() - 1, postSearch.getSize());
        Page<Post> posts = postService.getAllPostsWithPagination(postSearch, pageRequest);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("currentPage", posts.getNumber() + 1);
        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("hasNext", posts.getNumber() + 1 < posts.getTotalPages());
        model.addAttribute("hasPrev", posts.getNumber() > 0);
        model.addAttribute("previous", Math.max(posts.getNumber(), 1));
        model.addAttribute("next", Math.min(posts.getNumber()+2, posts.getTotalPages()));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)){
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                User user = userService.findByUsername(userDetails.getUsername());
                model.addAttribute("user", user);
            } else {
                log.warn("인증된 사용자가 CustomUserDetails 타입이 아닙니다: " + principal.getClass().getName());
            }
        }

        return "index";
    }

    @GetMapping("/posts/write")
    public String writePost(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User loginUser = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("user", loginUser);
        }
        return "posts/posts-write";
    }

    // 게시글 상세보기
    @GetMapping("/posts/getPost/{id}")
    public String getPost(@PathVariable Long id, Model model){
        PostResponse post = postService.get(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        List<Comment> comments = post.getComments();

        if(comments != null && !comments.isEmpty()){
            model.addAttribute("comments", comments);
        }

        if(user != null){
            model.addAttribute("user", user);
            if(post.getWriter().equals(user.getNickname())){
                model.addAttribute("writer", true);
            }
            else{
                model.addAttribute("writer", false);
            }
        }

        model.addAttribute("posts",post);

        return "posts/posts-read";
    }

    @GetMapping("/posts/update/{id}")
    public String updatePost(Model model, @PathVariable Long id){
        PostResponse postResponse = postService.get(id);

        model.addAttribute("posts",postResponse);

        return "posts/posts-update";
    }
}
