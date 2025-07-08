package pookyBlog.Service.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pookyBlog.Repository.HotCommentCountRepository;
import pookyBlog.event.Event;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.CommentDeletedEventPayload;
import pookyBlog.utils.TimeCalculatorUtils;

@Component
@RequiredArgsConstructor
public class CommentDeletedEventHandler implements EventHandler<CommentDeletedEventPayload>{
    private final HotCommentCountRepository commentCountRepository;

    @Override
    public void handle(Event<CommentDeletedEventPayload> event) {
        CommentDeletedEventPayload payload = event.getPayload();
        commentCountRepository.createOrUpdate(
                payload.getPostId(),
                payload.getPostCommentCount(),
                TimeCalculatorUtils.calculatorDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<CommentDeletedEventPayload> event) {
        return EventType.COMMENT_DELETED == event.getType();
    }

    @Override
    public Long findPostId(Event<CommentDeletedEventPayload> event) {
        return event.getPayload().getPostId();
    }
}
