package pookyBlog.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import pookyBlog.event.Event;
import pookyBlog.event.EventPayload;
import pookyBlog.event.EventType;
import pookyBlog.service.PostReadService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostReadEventConsumer {
    private final PostReadService postReadService;

    @KafkaListener(topics = {
            EventType.Topic.PookyBlog_Post,
            EventType.Topic.PookyBlog_Comment,
            EventType.Topic.PookyBlog_Like
    })
    public void listen(String message, Acknowledgment ack){
        log.info("[PostReadEventConsumer.listen] message={}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if(event != null){
            postReadService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
