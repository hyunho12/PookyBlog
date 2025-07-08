package pookyBlog.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import pookyBlog.Service.HotPostService;
import pookyBlog.event.Event;
import pookyBlog.event.EventPayload;
import pookyBlog.event.EventType;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotPostEventConsumer {
    private final HotPostService hotPostService;

    @KafkaListener(topics = {
            EventType.Topic.PookyBlog_Post,
            EventType.Topic.PookyBlog_Comment,
            EventType.Topic.PookyBlog_Like,
            EventType.Topic.PookyBlog_View
    })
    public void listen(String message, Acknowledgment ack){
        Event<EventPayload> event = Event.fromJson(message);
        if(event != null){
            hotPostService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
