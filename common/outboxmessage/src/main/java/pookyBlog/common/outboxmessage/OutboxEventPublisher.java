package pookyBlog.common.outboxmessage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventPayload;
import pookyBlog.common.event.EventType;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher { // 서비스로직에서 요청온 Event을 outbox테이블로 전송
    private final Snowflake snowflake;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(EventType type, EventPayload payload, Long shardKey){
        Outbox outbox = Outbox.create(
                snowflake.nextId(),
                type,
                Event.of(
                        snowflake.nextId(), type, payload
                ).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT
        );
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
