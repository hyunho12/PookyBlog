package pookyBlog.Service.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.Repository.PostLikeCountRepository;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostLikedEventPayload;
import pookyBlog.utils.TimeCalculatorUtils;

@Component
@RequiredArgsConstructor
public class PostLikedEventHandler implements EventHandler<PostLikedEventPayload>{
    private final PostLikeCountRepository postLikeCountRepository;

    @Override
    public void handle(Event<PostLikedEventPayload> event) {
        PostLikedEventPayload payload = event.getPayload();
        postLikeCountRepository.createOrUpdate(
                payload.getPostId(),
                payload.getPostLikeCount(),
                TimeCalculatorUtils.calculatorDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<PostLikedEventPayload> event) {
        return EventType.POST_LIKED == event.getType();
    }

    @Override
    public Long findPostId(Event<PostLikedEventPayload> event) {
        return event.getPayload().getPostLikeId();
    }
}
