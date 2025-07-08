package pookyBlog.Service.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.Repository.PostLikeCountRepository;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostUnlikedEventPayload;
import pookyBlog.utils.TimeCalculatorUtils;

@Component
@RequiredArgsConstructor
public class PostUnlikedEventHandler implements EventHandler<PostUnlikedEventPayload>{
    private final PostLikeCountRepository postLikeCountRepository;

    @Override
    public void handle(Event<PostUnlikedEventPayload> event) {
        PostUnlikedEventPayload payload = event.getPayload();
        postLikeCountRepository.createOrUpdate(
                payload.getPostId(),
                payload.getPostLikeCount(),
                TimeCalculatorUtils.calculatorDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<PostUnlikedEventPayload> event) {
        return EventType.POST_UNLIKED == event.getType();
    }

    @Override
    public Long findPostId(Event<PostUnlikedEventPayload> event) {
        return event.getPayload().getPostLikeId();
    }
}
