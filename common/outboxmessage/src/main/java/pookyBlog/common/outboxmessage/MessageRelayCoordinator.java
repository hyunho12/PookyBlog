package pookyBlog.common.outboxmessage;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator { // 여러 대의 서버가 중복으로 일하지 않도록, 각자 담당할 구역(샤드)을 정해주는 중앙 관제실 역할을 합니다.
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    private final String APP_ID = UUID.randomUUID().toString();

    private final int PING_INTERVAL_SECONDS = 3;
    private final int PING_FAILURE_THRESHOLD = 3;

    // AssignedShard 클래스를 이용해 현재 살아있는 서버 목록과 전체 샤드 개수를 기반으로 **"내가 처리해야 할 샤드 목록"**을 계산하여 반환합니다.
    public AssignedShard assignedShard(){
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHARD_COUNT);
    }

    // Redis에서 현재 살아있는 모든 서버의 ID 목록을 조회합니다.
    private List<String> findAppIds(){
        return redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1).stream()
                .sorted()
                .toList();
    }

    // @Scheduled
    //각 서버는 주기적으로 Redis에 "나 살아있어!"라는 생존 신호를 보냅니다.
    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping(){
        redisTemplate.executePipelined((RedisCallback<?>) action ->{
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();
            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID);
            conn.zRemRangeByScore(
                    key,
                    Double.NEGATIVE_INFINITY,
                    Instant.now().minusSeconds(PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            );
            return null;
        });
    }

    // 서버가 정상적으로 종료될 때 Redis에 등록된 자신의 ID를 삭제하여, 다른 서버들이 즉시 작업을 재분배할 수 있도록 합니다.
    @PreDestroy
    public void leave(){
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }

    private String generateKey(){
        return "message-relay-coordinator::app-list::%s".formatted(applicationName);
    }
}
