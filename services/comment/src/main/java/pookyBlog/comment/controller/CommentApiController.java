package pookyBlog.comment.controller;

import pookyBlog.common.Dto.Request.CommentCreate;
import jakarta.validation.Valid;
import pookyBlog.common.Dto.Request.CommentUpdate;
import pookyBlog.common.Dto.Response.CommentResponse;
import pookyBlog.comment.Service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class CommentApiController {
    private final CommentService commentService;

    @PostMapping("/comments/create")
    public ResponseEntity<Long> createComment(@RequestBody CommentCreate commentCreate){
        Long commentId = commentService.create(commentCreate);
        return ResponseEntity.ok(commentId);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable("postId") Long postId){
        return ResponseEntity.ok(commentService.getComment(postId));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId, @RequestParam Long userId,
                                              @Valid @RequestBody CommentUpdate commentUpdate){
        commentService.update(commentId, userId, commentUpdate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        commentService.delete(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/comments/post/{postId}/count")
    public Long count(@PathVariable("postId") Long postId){
        return commentService.count(postId);
    }
}
