package pookyBlog.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.PostCreatedEventPayload;
import pookyBlog.repository.PostQueryModel;
import pookyBlog.repository.PostQueryModelRepository;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PostCreatedEventHandler implements EventHandler<PostCreatedEventPayload>{
    private final PostQueryModelRepository postQueryModelRepository;

    @Override
    public void handle(Event<PostCreatedEventPayload> event) {
        PostCreatedEventPayload payload = event.getPayload();
        postQueryModelRepository.create(
                PostQueryModel.create(payload),
                Duration.ofDays(1)
        );
    }

    @Override
    public boolean supports(Event<PostCreatedEventPayload> event) {
        return EventType.POST_CREATED == event.getType();
    }
}
