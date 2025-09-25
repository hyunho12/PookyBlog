package pookyBlog.Service.eventhandler;

import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventPayload;

public interface EventHandler <T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
    Long findPostId(Event<T> event);
}
