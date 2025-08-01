package pookyBlog.controller;

import pookyBlog.Dto.Request.CommentCreate;
import pookyBlog.Dto.Request.CommentUpdate;
import pookyBlog.Entity.Comment;
import pookyBlog.Service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pookyBlog.Service.PostService;

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

    @GetMapping("/comments/post/{postId}/count")
    public Long count(@PathVariable("postId") Long postId){
        return commentService.count(postId);
    }
}
