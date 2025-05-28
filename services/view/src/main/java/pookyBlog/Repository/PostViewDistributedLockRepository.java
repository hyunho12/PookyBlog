package pookyBlog.Repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class PostViewDistributedLockRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "post::post::%s::user::%s::lock";

    public boolean lock(Long postId, Long userId, Duration ttl) {
        String key = generateKey(postId, userId);
        return redisTemplate.opsForValue().setIfAbsent(key, "", ttl); // key가 존재하지않다면 true , 존재하면 false
    }

    private String generateKey(Long postId, Long userId){
        return KEY_FORMAT.formatted(postId, userId);
    }
}
