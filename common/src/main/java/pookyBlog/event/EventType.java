package pookyBlog.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pookyBlog.event.payload.*;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    POST_CREATED(PostCreatedEventPayload.class, Topic.PookyBlog_Post),
    POST_UPDATED(PostUpdatedEventPayload.class, Topic.PookyBlog_Post),
    POST_DELETED(PostDeletedEventPayload.class, Topic.PookyBlog_Post),
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.PookyBlog_Comment),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.PookyBlog_Comment),
    POST_VIEWED(PostViewedEventPayload.class, Topic.PookyBlog_View),
    POST_LIKED(PostLikedEventPayload.class, Topic.PookyBlog_Like),
    POST_UNLIKED(PostUnlikedEventPayload.class, Topic.PookyBlog_Like);

    private final Class<? extends EventPayload> payloadClass;
    private final String topics;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic{
        public static final String PookyBlog_Post = "pooky-board-post";
        public static final String PookyBlog_Comment = "pooky-board-comment";
        public static final String PookyBlog_View = "pooky-board-view";
        public static final String PookyBlog_Like = "pooky-board-like";
    }
}
