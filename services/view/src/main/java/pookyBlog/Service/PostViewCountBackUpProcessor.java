package pookyBlog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.Entity.PostViewCount;
import pookyBlog.Repository.PostViewCountBackUpRepository;
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.common.outboxmessage.OutboxRepository;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostViewedEventPayload;

@Service
@RequiredArgsConstructor
public class PostViewCountBackUpProcessor {
    private final PostViewCountBackUpRepository postViewCountBackUpRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final OutboxRepository outboxRepository;

    @Transactional
    public void backUp(Long postId, Long viewCount){
        int result = postViewCountBackUpRepository.updateViewCount(postId, viewCount);
        if(result == 0){
            postViewCountBackUpRepository.findById(postId)
                    .ifPresentOrElse(ignored -> { },
                            () -> postViewCountBackUpRepository.save(PostViewCount.builder()
                                            .id(postId)
                                            .viewCount(viewCount)
                                    .build()));
        }

        outboxEventPublisher.publish(
                EventType.POST_VIEWED,
                PostViewedEventPayload.builder()
                        .postId(postId)
                        .postViewCount(viewCount)
                        .build(),
                0L
        );
    }
}
