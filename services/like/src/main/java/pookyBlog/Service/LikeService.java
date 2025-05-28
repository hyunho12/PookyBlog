package pookyBlog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.Entity.Like;
import pookyBlog.Entity.LikeCount;
import pookyBlog.Entity.Post;
import pookyBlog.Entity.User;
import pookyBlog.Repository.LikeRepository;
import pookyBlog.Repository.PostRepository;
import pookyBlog.Repository.UserRepository;
import pookyBlog.common.snowflake.Snowflake;

@RequiredArgsConstructor
@Service
public class LikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final Snowflake snowflake;

    @Transactional
    public void likePost(Long userId, Long postId){
        if(likeRepository.existsByUserIdAndPostId(userId, postId)){
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Like like = Like.builder()
                .id(snowflake.nextId())
                .user(user)
                .post(post)
                .build();

        likeRepository.save(like);
        likeRepository.increaseLikeCount(postId);
    }

    @Transactional
    public void unlikePost(Long userId, Long postId){
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누르지 않은 게시글입니다."));

        likeRepository.delete(like);
        likeRepository.decreaseLikeCount(postId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikePost(Long userId, Long postId){
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Transactional(readOnly = true)
    public long getPostLikeCount(Long postId){
        return likeRepository.countByPostId(postId);
    }

    @Transactional
    public void likePostWithPessimisticLock(Long userId, Long postId){
        // 좋아요 저장
        likePost(userId, postId);

        // 좋아요수 증가
        LikeCount likeCount = likeRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new RuntimeException("LikeCount 없음"));
        likeCount.increaseLikeCount();
    }

    @Transactional
    public void likePostWithOptimisticLock(Long userId, Long postId){
        likePost(userId, postId);

        LikeCount likeCount = likeRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("LikeCount 없음"));

        likeCount.increaseLikeCount();
    }

    @Transactional
    public void likePostWithPlusQuery(Long userId, Long postId){
        likePost(userId, postId);
        likeRepository.increaseLikeCount(postId);
    }

    @Transactional
    public void likePostWithMinusQuery(Long userId, Long postId){
        likePost(userId, postId);
        likeRepository.decreaseLikeCount(postId);
    }
}
