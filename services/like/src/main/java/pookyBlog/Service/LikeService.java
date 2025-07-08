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
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostLikedEventPayload;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class LikeService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final Snowflake snowflake;
    private final OutboxEventPublisher outboxEventPublisher;

    public Like saveLike(Long postId, Long userId) {
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

        return likeRepository.save(like);
    }

    public Like deleteLike(Long postId, Long userId) {
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누르지 않은 게시글입니다."));

        likeRepository.delete(like);
        return like;
    }

    @Transactional
    public void likePost(Long userId, Long postId){
        Like like = saveLike(userId, postId);
        likeRepository.increaseLikeCount(postId);
        publishLikeEvent(EventType.POST_LIKED, like);
    }

    @Transactional
    public void unlikePost(Long userId, Long postId){
        Like unlike = deleteLike(postId, userId);
        likeRepository.decreaseLikeCount(postId);
        publishLikeEvent(EventType.POST_UNLIKED, unlike);
    }

    @Transactional
    public void likePostWithPessimisticLock(Long userId, Long postId){
        // 좋아요 저장
        Like like = saveLike(userId, postId);

        // 좋아요수 증가
        LikeCount likeCount = likeRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new RuntimeException("LikeCount 없음"));
        likeCount.increaseLikeCount();
        publishLikeEvent(EventType.POST_LIKED, like);
    }

    @Transactional
    public void unlikePostWithPessimisticLock(Long userId, Long postId){
        Like unlike = deleteLike(userId, postId);
        LikeCount likeCount = likeRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new RuntimeException("LikeCount 없음"));
        likeCount.decreaseLikeCount();
        publishLikeEvent(EventType.POST_UNLIKED, unlike);

    }

    @Transactional
    public void likePostWithOptimisticLock(Long userId, Long postId){
        Like like = saveLike(userId, postId);

        LikeCount likeCount = likeRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("LikeCount 없음"));

        likeCount.increaseLikeCount();
        publishLikeEvent(EventType.POST_LIKED, like);

    }

    @Transactional
    public void unlikePostWithOptimisticLock(Long userId, Long postId){
        Like unlike = deleteLike(userId, postId);
        LikeCount likeCount = likeRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("LikeCount 없음"));
        likeCount.decreaseLikeCount();
        publishLikeEvent(EventType.POST_UNLIKED, unlike);
    }

    @Transactional
    public void likePostWithPlusQuery(Long userId, Long postId){
        Like like = saveLike(userId, postId);
        likeRepository.increaseLikeCount(postId);
        publishLikeEvent(EventType.POST_LIKED, like);
    }

    @Transactional
    public void likePostWithMinusQuery(Long userId, Long postId){
        Like unlike = deleteLike(userId, postId);
        likeRepository.decreaseLikeCount(postId);
        publishLikeEvent(EventType.POST_LIKED, unlike);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikePost(Long userId, Long postId){
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Transactional(readOnly = true)
    public long getPostLikeCount(Long postId){
        return likeRepository.countByPostId(postId);
    }

    // 이벤트 발행 공통 메서드
    private void publishLikeEvent(EventType eventType, Like like) {
        outboxEventPublisher.publish(
                eventType,
                PostLikedEventPayload.builder()
                        .postLikeId(like.getId())
                        .postId(like.getPost().getId())
                        .userId(like.getUser().getId())
                        .createdAt(LocalDateTime.parse(like.getCreatedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .postLikeCount(likeRepository.countByPostId(like.getPost().getId()))
                        .build(),
                0L
        );
    }
}
