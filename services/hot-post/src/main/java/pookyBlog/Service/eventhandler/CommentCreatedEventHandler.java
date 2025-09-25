package pookyBlog.Service.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.Repository.HotCommentCountRepository;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.CommentCreatedEventPayload;
import pookyBlog.utils.TimeCalculatorUtils;

@Component
@RequiredArgsConstructor
public class CommentCreatedEventHandler implements EventHandler<CommentCreatedEventPayload>{
    private final HotCommentCountRepository hotCommentCountRepository;

    @Override
    public void handle(Event<CommentCreatedEventPayload> event) {
        CommentCreatedEventPayload payload = event.getPayload();
        hotCommentCountRepository.createOrUpdate(
                payload.getPostId(),
                payload.getPostCommentCount(),
                TimeCalculatorUtils.calculatorDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<CommentCreatedEventPayload> event) {
        return EventType.COMMENT_CREATED == event.getType();
    }

    @Override
    public Long findPostId(Event<CommentCreatedEventPayload> event) {
        return event.getPayload().getPostId();
    }
}
