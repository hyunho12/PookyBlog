package pookyBlog.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.Entity.PostViewCount;
import pookyBlog.ViewApplication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = ViewApplication.class)
class PostViewCountRepositoryTest {
    @Autowired
    PostViewCountBackUpRepository postViewCountBackUpRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    @Transactional
    void updateViewCountTest(){
        // given
        postViewCountBackUpRepository.save(
                PostViewCount.builder()
                        .id(1L)
                        .viewCount(0L)
                        .build()
        );
        entityManager.flush();
        entityManager.clear();

        // when
        int result1 = postViewCountBackUpRepository.updateViewCount(1L, 100L);
        int result2 = postViewCountBackUpRepository.updateViewCount(1L, 300L);
        int result3 = postViewCountBackUpRepository.updateViewCount(1L, 200L);

        // then
        assertThat(result1).isEqualTo(1);
        assertThat(result2).isEqualTo(1);
        assertThat(result3).isEqualTo(0);

        PostViewCount articleViewCount = postViewCountBackUpRepository.findById(1L).get();
        assertThat(articleViewCount.getViewCount()).isEqualTo(300L);
    }
}
