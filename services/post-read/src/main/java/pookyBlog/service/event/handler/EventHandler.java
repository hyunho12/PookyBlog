package pookyBlog.service.event.handler;

import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
