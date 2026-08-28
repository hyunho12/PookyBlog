package pookyBlog.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pookyBlog.comment.Repository.CommentRepository;
import pookyBlog.common.outboxmessage.OutboxRepository;
import pookyBlog.post.Repository.PostCountRepository;
import pookyBlog.post.Repository.PostRepository;
import pookyBlog.user.Repository.UserRepository;

@SpringBootApplication(
        scanBasePackages = {"pookyBlog.comment", "pookyBlog.common"}
)
@EntityScan(basePackages = {"pookyBlog.common.Entity", "pookyBlog.common.outboxmessage"})
@EnableJpaRepositories(
        basePackageClasses = {
                CommentRepository.class,
                UserRepository.class,
                PostRepository.class,
                OutboxRepository.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = PostCountRepository.class
        )
)
public class CommentApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentApplication.class, args);
    }
}
