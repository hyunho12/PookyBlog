package pookyBlog.comment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pookyBlog.common.Entity.Comment;
import pookyBlog.common.Entity.Post;
import pookyBlog.common.Entity.User;
import pookyBlog.comment.Repository.CommentRepository;
import pookyBlog.common.outboxmessage.OutboxRepository;
import pookyBlog.post.Repository.PostCountRepository;
import pookyBlog.post.Repository.PostRepository;
import pookyBlog.user.Repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

class CommentApplicationConfigurationTest {

    @Test
    void scansTheActualCommonEntityPackage() {
        EntityScan entityScan = CommentApplication.class.getAnnotation(EntityScan.class);

        assertThat(entityScan).isNotNull();
        assertThat(entityScan.basePackages())
                .containsExactly("pookyBlog.common.Entity", "pookyBlog.common.outboxmessage");
    }

    @Test
    void commentRelationsUseEntitiesFromTheSameScannedPackage() throws NoSuchFieldException {
        assertThat(Comment.class.getDeclaredField("posts").getType()).isEqualTo(Post.class);
        assertThat(Comment.class.getDeclaredField("user").getType()).isEqualTo(User.class);
    }

    @Test
    void registersOnlyTheRepositoriesRequiredByCommentPersistence() {
        EnableJpaRepositories repositories = CommentApplication.class
                .getAnnotation(EnableJpaRepositories.class);

        assertThat(repositories).isNotNull();
        assertThat(repositories.basePackageClasses()).containsExactly(
                CommentRepository.class,
                UserRepository.class,
                PostRepository.class,
                OutboxRepository.class
        );
        assertThat(repositories.excludeFilters()).singleElement().satisfies(filter -> {
            assertThat(filter.type()).isEqualTo(FilterType.ASSIGNABLE_TYPE);
            assertThat(filter.classes()).containsExactly(PostCountRepository.class);
        });
    }
}
