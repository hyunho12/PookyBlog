package pookyBlog.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.PostDeletedEventPayload;
import pookyBlog.repository.PostQueryModelRepository;

@Component
@RequiredArgsConstructor
public class PostDeletedEventHandler implements EventHandler<PostDeletedEventPayload>{
    private final PostQueryModelRepository postQueryModelRepository;

    @Override
    public void handle(Event<PostDeletedEventPayload> event) {
        PostDeletedEventPayload payload = event.getPayload();
        postQueryModelRepository.delete(payload.getPostId());
    }

    @Override
    public boolean supports(Event<PostDeletedEventPayload> event) {
        return EventType.POST_DELETED == event.getType();
    }
}
