package pookyBlog.Repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class PostLikeCountRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "hot-post::post::%s::like-count";

    public void createOrUpdate(Long postId, Long likeCount, Duration ttl){
        redisTemplate.opsForValue().set(generateKey(postId), String.valueOf(likeCount), ttl);
    }

    public Long get(Long postId){
        String result = redisTemplate.opsForValue().get(generateKey(postId));
        return result == null ? 0L : Long.valueOf(result);
    }

    private String generateKey(Long postId){
        return KEY_FORMAT.formatted(postId);
    }
}
