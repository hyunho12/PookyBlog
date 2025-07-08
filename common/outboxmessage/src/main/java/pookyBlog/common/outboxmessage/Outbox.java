package pookyBlog.common.outboxmessage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import pookyBlog.event.EventType;

import java.time.LocalDateTime;

@Table(name = "outbox")
@ToString
@Getter
@Entity
public class Outbox {
    @Id
    private Long outboxId;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private String payload;
    private Long shardKey;
    private LocalDateTime createdAt;

    public static Outbox create(Long outboxId, EventType eventType, String payload, Long shardKey){
        Outbox outbox = new Outbox();
        outbox.outboxId = outboxId;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.shardKey = shardKey;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }
}
