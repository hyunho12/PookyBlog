package pookyBlog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import pookyBlog.dataserializer.DataSerializer;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostQueryModelRepository {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "post-read::post::%s";

    public void create(PostQueryModel postQueryModel, Duration ttl){
        redisTemplate.opsForValue()
                .set(generateKey(postQueryModel), DataSerializer.serialize(postQueryModel), ttl);
    }

    public void update(PostQueryModel postQueryModel){
        redisTemplate.opsForValue()
                .setIfPresent(generateKey(postQueryModel), DataSerializer.serialize(postQueryModel));
    }

    public void delete(Long postId){
        redisTemplate.delete(generateKey(postId));
    }

    public Optional<PostQueryModel> read(Long postId){
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(postId))
        ).map(json -> DataSerializer.deserialize(json, PostQueryModel.class));
    }

    private String generateKey(PostQueryModel postQueryModel){
        return generateKey(postQueryModel.getPostId());
    }

    private String generateKey(Long postId){
        return KEY_FORMAT.formatted(postId);
    }
}
