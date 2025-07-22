package pookyBlog.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostLikedEventPayload;
import pookyBlog.repository.PostQueryModelRepository;

@Component
@RequiredArgsConstructor
public class PostLikeEventHandler implements EventHandler<PostLikedEventPayload> {
    private final PostQueryModelRepository postQueryModelRepository;

    @Override
    public void handle(Event<PostLikedEventPayload> event) {
        postQueryModelRepository.read(event.getPayload().getPostId())
                .ifPresent(postQueryModel -> {
                    postQueryModel.update(event.getPayload());
                    postQueryModelRepository.update(postQueryModel);
                });
    }

    @Override
    public boolean supports(Event<PostLikedEventPayload> event) {
        return EventType.POST_LIKED == event.getType();
    }
}
