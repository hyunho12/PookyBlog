package pookyBlog.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.CommentCreatedEventPayload;
import pookyBlog.repository.PostQueryModelRepository;

@Component
@RequiredArgsConstructor
public class CommentCreatedEventHandler implements EventHandler<CommentCreatedEventPayload>{
    private final PostQueryModelRepository postQueryModelRepository;

    @Override
    public void handle(Event<CommentCreatedEventPayload> event) {
        postQueryModelRepository.read(event.getPayload().getPostId())
                .ifPresent(postQueryModel -> {
                    postQueryModel.update(event.getPayload());
                    postQueryModelRepository.update(postQueryModel);
                });
    }

    @Override
    public boolean supports(Event<CommentCreatedEventPayload> event) {
        return EventType.COMMENT_CREATED == event.getType();
    }
}
