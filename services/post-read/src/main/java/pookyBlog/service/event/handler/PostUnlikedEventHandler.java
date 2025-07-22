package pookyBlog.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostUnlikedEventPayload;
import pookyBlog.repository.PostQueryModelRepository;

@Component
@RequiredArgsConstructor
public class PostUnlikedEventHandler implements EventHandler<PostUnlikedEventPayload> {
    private final PostQueryModelRepository postQueryModelRepository;

    @Override
    public void handle(Event<PostUnlikedEventPayload> event) {
        postQueryModelRepository.read(event.getPayload().getPostId())
                .ifPresent(postQueryModel -> {
                    postQueryModel.update(event.getPayload());
                    postQueryModelRepository.update(postQueryModel);
                });
    }

    @Override
    public boolean supports(Event<PostUnlikedEventPayload> event) {
        return EventType.POST_UNLIKED == event.getType();
    }
}
