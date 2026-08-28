package pookyBlog.post;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.common.Dto.Request.PostUpdate;
import pookyBlog.common.Entity.Post;
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.post.Entity.PostCount;
import pookyBlog.post.Repository.PostCountRepository;
import pookyBlog.post.Repository.PostRepository;
import pookyBlog.post.Service.PostService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PostApplication.class)
@Transactional
class PostServiceMutationTest {
    @Autowired PostService postService;
    @Autowired PostRepository postRepository;
    @Autowired PostCountRepository postCountRepository;
    @Autowired Snowflake snowflake;
    @Autowired EntityManager entityManager;
    @MockitoBean OutboxEventPublisher outboxEventPublisher;

    @Test
    void updateIsCommittedToManagedEntityAndVisibleAfterRepositoryReload() {
        Long id = snowflake.nextId();
        postRepository.saveAndFlush(Post.builder()
                .id(id).title("before").content("before content").writer("writer").build());

        postService.update(id, PostUpdate.builder()
                .title("after").content("after content").build());
        postRepository.flush();
        entityManager.clear();

        Post reloaded = postRepository.findById(id).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("after");
        assertThat(reloaded.getContent()).isEqualTo("after content");
    }

    @Test
    void deleteRemovesPostFromRepository() {
        Long id = snowflake.nextId();
        postRepository.saveAndFlush(Post.builder()
                .id(id).title("delete").content("delete content").writer("writer").build());
        postCountRepository.saveAndFlush(new PostCount(id, 1L));

        postService.delete(id);
        postRepository.flush();
        entityManager.clear();

        assertThat(postRepository.findById(id)).isEmpty();
    }
}
