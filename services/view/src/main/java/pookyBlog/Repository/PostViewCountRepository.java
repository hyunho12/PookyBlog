package pookyBlog.Repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostViewCountRepository {
    private final StringRedisTemplate redisTemplate;

    // view::post::{post_id}::view_count
    private static final String KEY_FORMAT = "view::post::{post_id}::view_count";

    public Long read(Long postId){
        String result = redisTemplate.opsForValue().get(generateKey(postId));
        return result == null ? 0L : Long.valueOf(result);
    }

    public Long increase(Long postId){
        return redisTemplate.opsForValue().increment(generateKey(postId), 1);
    }

    private String generateKey(Long postId){
        return KEY_FORMAT.formatted(postId);
    }
}
