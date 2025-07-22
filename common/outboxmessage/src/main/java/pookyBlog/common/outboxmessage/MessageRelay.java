package pookyBlog.common.outboxmessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay { // outbox테이블에서 outbox을꺼내 kafka로 전달하는 역할
    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> messageRelayKafkaTemplate;
    private final OutboxDeleteService outboxDeleteService;

    // DB 트랜잭션이 커밋되기 직전에 Outbox 데이터를 DB에 저장
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent){
        log.info("[MessageRelay.createOutbox] outboxEvent={}",outboxEvent);
        outboxRepository.save(outboxEvent.getOutbox());
    }

    //@Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent){
        publishEvent(outboxEvent.getOutbox());
    }

    // DB 트랜잭션이 성공적으로 커밋된 직후에 실행됩니다. 메시지 발행을 즉시 시도하여 성공하면 outbox 테이블에서 데이터를 삭제
    private void publishEvent(Outbox outbox){
        try{
            messageRelayKafkaTemplate.send(
                    outbox.getEventType().getTopics(),
                    String.valueOf(outbox.getShardKey()),
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS);
            log.info("Deleting outbox id: {}", outbox.getOutboxId());
            //outboxRepository.delete(outbox);
            outboxDeleteService.deleteOutbox(outbox);
            log.info("[MessageRelay.publishEvent] Success: outboxId={}", outbox.getOutboxId());
        } catch (Exception e){
            log.error("[MessageRelay.publishEvent] outbox={}",outbox,e);
        }
    }

    // 만약 위 publishEvent가 실패하면 outbox에 데이터가 남습니다. 이 스케줄러는 주기적으로 돌면서 아직 처리되지 않은 물품들을
    // 찾아내 재발송을 시도합니다.
    @Scheduled(
            fixedDelay = 10,
            initialDelay = 5,
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor"
    )
    public void publishPendingEvent(){
        AssignedShard assignedShard = messageRelayCoordinator.assignedShard();
        log.info("[MessageRelay.publishPendingEvent] assignShard size={}", assignedShard.getShards().size());
        for(Long shard : assignedShard.getShards()){
            List<Outbox> outboxes = outboxRepository.findOutboxes(
                    shard,
                    LocalDateTime.now().minusSeconds(10),
                    Pageable.ofSize(100)
            );
            for(Outbox outbox : outboxes){
                publishEvent(outbox);
            }
        }
    }
}
