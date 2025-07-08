package pookyBlog.Service.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.Repository.PostCreatedTimeRepository;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostCreatedEventPayload;
import pookyBlog.utils.TimeCalculatorUtils;

@Component
@RequiredArgsConstructor
public class PostCreateEventHandler implements EventHandler<PostCreatedEventPayload>{
    private final PostCreatedTimeRepository postCreatedTimeRepository;

    @Override
    public void handle(Event<PostCreatedEventPayload> event) {
        PostCreatedEventPayload payload = event.getPayload();
        postCreatedTimeRepository.createOrUpdate(
                payload.getPostId(),
                payload.getCreatedAt(),
                TimeCalculatorUtils.calculatorDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<PostCreatedEventPayload> event) {
        return EventType.POST_CREATED == event.getType();
    }

    @Override
    public Long findPostId(Event<PostCreatedEventPayload> event) {
        return event.getPayload().getPostId();
    }
}
