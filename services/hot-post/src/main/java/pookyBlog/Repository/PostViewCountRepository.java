package pookyBlog.Repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class PostViewCountRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "hot-post::post::{postId}::view-count";

    public void createOrUpdate(Long postId, Long viewCount, Duration ttl){
        redisTemplate.opsForValue().set(generateKey(postId), String.valueOf(viewCount), ttl);
    }

    public Long get(Long postId){
        String result = redisTemplate.opsForValue().get(generateKey(postId));
        return result == null ? 0L : Long.valueOf(result);
    }

    private String generateKey(Long postId){
        return KEY_FORMAT.formatted(postId);
    }
}
