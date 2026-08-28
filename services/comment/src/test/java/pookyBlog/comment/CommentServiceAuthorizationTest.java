package pookyBlog.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import pookyBlog.comment.Repository.CommentRepository;
import pookyBlog.comment.Service.CommentService;
import pookyBlog.common.Dto.Request.CommentUpdate;
import pookyBlog.common.Entity.Comment;
import pookyBlog.common.Entity.Post;
import pookyBlog.common.Entity.User;
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.post.Repository.PostRepository;
import pookyBlog.user.Repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CommentServiceAuthorizationTest {
    private static final long COMMENT_ID = 349103684996575232L;
    private static final long OWNER_ID = 348523324499824640L;
    private static final long OTHER_USER_ID = 348523324499824641L;
    private CommentRepository commentRepository;
    private CommentService commentService;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        commentService = new CommentService(commentRepository, mock(UserRepository.class),
                mock(PostRepository.class), mock(OutboxEventPublisher.class), mock(Snowflake.class));
        User owner = User.builder().id(OWNER_ID).nickname("pooky").build();
        Post post = Post.builder().id(1L).writer("pooky").build();
        comment = Comment.builder().id(COMMENT_ID).comments("before").posts(post).user(owner).build();
        comment.onPrePersist();
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
    }

    @Test
    void ownerCanUpdateComment() {
        CommentUpdate update = new CommentUpdate();
        update.setContent("after");
        commentService.update(COMMENT_ID, OWNER_ID, update);
        assertThat(comment.getComments()).isEqualTo("after");
    }

    @Test
    void ownerCanDeleteComment() {
        commentService.delete(COMMENT_ID, OWNER_ID);
        verify(commentRepository).delete(comment);
    }

    @Test
    void otherUserCannotUpdateOrDeleteComment() {
        CommentUpdate update = new CommentUpdate();
        update.setContent("after");
        assertThatThrownBy(() -> commentService.update(COMMENT_ID, OTHER_USER_ID, update))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> commentService.delete(COMMENT_ID, OTHER_USER_ID))
                .isInstanceOf(AccessDeniedException.class);
        verify(commentRepository, never()).delete(any());
    }
}
