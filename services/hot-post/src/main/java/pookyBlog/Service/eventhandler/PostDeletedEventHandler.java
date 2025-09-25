package pookyBlog.Service.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.Repository.HotPostListRepository;
import pookyBlog.Repository.PostCreatedTimeRepository;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.PostDeletedEventPayload;

@Component
@RequiredArgsConstructor
public class PostDeletedEventHandler implements EventHandler<PostDeletedEventPayload>{
    private final HotPostListRepository hotPostListRepository;
    private final PostCreatedTimeRepository postCreatedTimeRepository;

    @Override
    public void handle(Event<PostDeletedEventPayload> event) {
        PostDeletedEventPayload payload = event.getPayload();
        postCreatedTimeRepository.delete(payload.getPostId());
        hotPostListRepository.remove(payload.getPostId(), payload.getCreatedAt());
    }

    @Override
    public boolean supports(Event<PostDeletedEventPayload> event) {
        return EventType.POST_DELETED == event.getType();
    }

    @Override
    public Long findPostId(Event<PostDeletedEventPayload> event) {
        return event.getPayload().getPostId();
    }
}
