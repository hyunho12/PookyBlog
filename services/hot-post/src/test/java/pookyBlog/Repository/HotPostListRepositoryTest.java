package pookyBlog.Repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HotPostListRepositoryTest {
    @Autowired
    HotPostListRepository hotPostListRepository;

    @Test
    void addTest() throws InterruptedException{
        // given
        LocalDateTime time = LocalDateTime.of(2025,7,3,0,0);
        long limit = 3;

        // when
        hotPostListRepository.add(1L, time, 2L, limit, Duration.ofSeconds(3));
        hotPostListRepository.add(2L, time, 3L, limit, Duration.ofSeconds(3));
        hotPostListRepository.add(3L, time, 1L, limit, Duration.ofSeconds(3));
        hotPostListRepository.add(4L, time, 5L, limit, Duration.ofSeconds(3));
        hotPostListRepository.add(5L, time, 4L, limit, Duration.ofSeconds(3));

        // then
        List<Long> postIds = hotPostListRepository.getAll("20250703");

        assertThat(postIds).hasSize(Long.valueOf(limit).intValue());
        assertThat(postIds.get(0)).isEqualTo(4);
        assertThat(postIds.get(1)).isEqualTo(5);
        assertThat(postIds.get(2)).isEqualTo(2);
    }
}