package pookyBlog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pookyBlog.Repository.HotPostListRepository;
import pookyBlog.Repository.PostCreatedTimeRepository;
import pookyBlog.Service.eventhandler.EventHandler;
import pookyBlog.event.Event;
import pookyBlog.event.EventPayload;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HotPostScoreUpdater {
    private final HotPostListRepository hotPostListRepository;
    private final HotPostScoreCalculator hotPostScoreCalculator;
    private final PostCreatedTimeRepository postCreatedTimeRepository;

    private static final long HOT_POST_COUNT = 10;
    private static final Duration HOT_POST_TTL = Duration.ofDays(10);

    public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler){
        Long postId = eventHandler.findPostId(event);
        LocalDateTime createdTime = postCreatedTimeRepository.get(postId);

        if(!isPostCreatedToday(createdTime)){
            return;
        }
        eventHandler.handle(event);

        long score = hotPostScoreCalculator.calculate(postId);
        hotPostListRepository.add(
                postId,
                createdTime,
                score,
                HOT_POST_COUNT,
                HOT_POST_TTL
        );
    }

    private boolean isPostCreatedToday(LocalDateTime createTime){
        return createTime != null && createTime.toLocalDate().equals(LocalDate.now());
    }

}
