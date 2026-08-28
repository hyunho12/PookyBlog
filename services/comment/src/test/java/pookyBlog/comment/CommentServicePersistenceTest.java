package pookyBlog.comment;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.comment.Repository.CommentRepository;
import pookyBlog.comment.Service.CommentService;
import pookyBlog.common.Dto.Request.CommentCreate;
import pookyBlog.common.Entity.Post;
import pookyBlog.common.Entity.Role;
import pookyBlog.common.Entity.User;
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.post.Repository.PostRepository;
import pookyBlog.user.Repository.UserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CommentApplication.class)
@Transactional
class CommentServicePersistenceTest {
    @Autowired CommentService commentService;
    @Autowired CommentRepository commentRepository;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired Snowflake snowflake;
    @Autowired EntityManager entityManager;
    @MockitoBean OutboxEventPublisher outboxEventPublisher;

    @Test
    void createCommitsAReadableCommentRow() {
        String suffix = UUID.randomUUID().toString().substring(0, 12);
        User user = userRepository.save(User.builder()
                .id(snowflake.nextId()).username("comment-" + suffix)
                .nickname("comment-nick-" + suffix).email(suffix + "@test.invalid")
                .password("password").role(Role.USER).build());
        Post post = postRepository.save(Post.builder()
                .id(snowflake.nextId()).title("post").content("content")
                .writer(user.getNickname()).build());
        CommentCreate request = new CommentCreate();
        request.setUserId(user.getId());
        request.setPostsId(post.getId());
        request.setComment("persisted comment");

        Long commentId = commentService.create(request);
        entityManager.flush();
        entityManager.clear();

        assertThat(commentRepository.findById(commentId))
                .get().extracting(comment -> comment.getComments())
                .isEqualTo("persisted comment");
        assertThat(commentService.getComment(post.getId()))
                .singleElement()
                .satisfies(comment -> {
                    assertThat(comment.getPostId()).isEqualTo(post.getId());
                    assertThat(comment.getUserId()).isEqualTo(user.getId());
                    assertThat(comment.getNickname()).isEqualTo(user.getNickname());
                    assertThat(comment.getComments()).isEqualTo("persisted comment");
                });
    }
}
