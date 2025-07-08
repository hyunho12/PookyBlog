package pookyBlog.Service.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.Repository.PostViewCountRepository;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostViewedEventPayload;
import pookyBlog.utils.TimeCalculatorUtils;

@Component
@RequiredArgsConstructor
public class PostViewedEventHandler implements EventHandler<PostViewedEventPayload>{
    private final PostViewCountRepository postViewCountRepository;

    @Override
    public void handle(Event<PostViewedEventPayload> event) {
        PostViewedEventPayload payload = event.getPayload();
        postViewCountRepository.createOrUpdate(
                payload.getPostId(),
                payload.getPostViewCount(),
                TimeCalculatorUtils.calculatorDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<PostViewedEventPayload> event) {
        return EventType.POST_VIEWED == event.getType();
    }

    @Override
    public Long findPostId(Event<PostViewedEventPayload> event) {
        return event.getPayload().getPostId();
    }
}
