package pookyBlog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pookyBlog.Service.eventhandler.EventHandler;
import pookyBlog.Service.response.HotPostResponse;
import pookyBlog.Repository.HotPostListRepository;
import pookyBlog.client.PostClient;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventPayload;
import pookyBlog.common.event.EventType;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HotPostService {
    private final PostClient postClient;
    private final List<EventHandler> eventHandlers;
    private final HotPostScoreUpdater hotPostScoreUpdater;
    private final HotPostListRepository hotPostListRepository;

    public void handleEvent(Event<EventPayload> event){
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if(eventHandler == null){
            return;
        }
        if(isPostCreatedOrDeleted(event)){
            eventHandler.handle(event);
        }
        else{
            hotPostScoreUpdater.update(event, eventHandler);
        }
    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event){
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    private boolean isPostCreatedOrDeleted(Event<EventPayload> event){
        return EventType.POST_CREATED == event.getType() || EventType.POST_DELETED == event.getType();
    }

    public List<HotPostResponse> getAll(String dateStr){
        return hotPostListRepository.getAll(dateStr).stream()
                .map(postClient::getPost)
                .filter(Objects::nonNull)
                .map(HotPostResponse::from)
                .toList();
    }
}
