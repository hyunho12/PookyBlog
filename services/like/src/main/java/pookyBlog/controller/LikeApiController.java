package pookyBlog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pookyBlog.Service.LikeService;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeApiController {
    private final LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<String> likePost(@RequestParam Long userId, @PathVariable Long postId){
        likeService.likePost(userId, postId);
        return ResponseEntity.ok("성공");
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> unlikePost(@RequestParam Long userId, @PathVariable Long postId){
        likeService.unlikePost(userId, postId);
        return ResponseEntity.ok("취소 성공");
    }

    @GetMapping("/exists/{postId}")
    public ResponseEntity<Boolean> hasUserLiked(@RequestParam Long userId, @PathVariable Long postId){
        return ResponseEntity.ok(likeService.hasUserLikePost(userId, postId));
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> countLikes(@PathVariable Long postId){
        return ResponseEntity.ok(likeService.getPostLikeCount(postId));
    }
}
