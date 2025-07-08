package pookyBlog.Repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Repository
@RequiredArgsConstructor
public class PostCreatedTimeRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "hot-post::post::%s::created-time";

    public void createOrUpdate(Long postId, LocalDateTime createAt, Duration ttl){
        redisTemplate.opsForValue().set(generateKey(postId), String.valueOf(createAt.toInstant(ZoneOffset.UTC).toEpochMilli()), ttl);
    }

    public void delete(Long postId){
        redisTemplate.delete(generateKey(postId));
    }

    public LocalDateTime get(Long postId){
        String result = redisTemplate.opsForValue().get(generateKey(postId));
        if(result == null){
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Long.valueOf(result)), ZoneOffset.UTC
        );
    }

    private String generateKey(Long postId){
        return KEY_FORMAT.formatted(postId);
    }
}
