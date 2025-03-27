package com.pookyBlog.pookyBlog.controller;

import com.pookyBlog.pookyBlog.Dto.Request.CommentCreate;
import com.pookyBlog.pookyBlog.Dto.Request.CommentUpdate;
import com.pookyBlog.pookyBlog.Entity.Comment;
import com.pookyBlog.pookyBlog.Service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CommentApiController {
    private final CommentService commentService;

    @PostMapping("/comments/create")
    public ResponseEntity<Long> createComment(@RequestBody CommentCreate commentCreate){
        Long commentId = commentService.create(commentCreate);
        return ResponseEntity.ok(commentId);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId){
        return ResponseEntity.ok(commentService.getComment(postId));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId, @RequestBody CommentUpdate commentUpdate){
        commentService.update(commentId, commentUpdate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.ok().build();
    }
}
