package pookyBlog.view.Repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PostViewCountRepositoryBatchTest {
    @Test
    void missingRedisKeyIsReturnedAsZeroInOneMultiGet() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.multiGet(List.of(
                "view::post::1::view_count",
                "view::post::2::view_count",
                "view::post::3::view_count")))
                .thenReturn(java.util.Arrays.asList("10", "5", null));
        PostViewCountRepository repository = new PostViewCountRepository(redisTemplate);

        assertThat(repository.readAll(List.of(1L, 2L, 3L)))
                .containsEntry(1L, 10L)
                .containsEntry(2L, 5L)
                .containsEntry(3L, 0L);
        verify(values, times(1)).multiGet(anyList());
        verify(values, never()).get(anyString());
    }
}
