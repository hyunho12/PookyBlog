package pookyBlog.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"pookyBlog.comment", "pookyBlog.common", "pookyBlog.user.Repository", "pookyBlog.post.Repository"}
)
@EntityScan(basePackages = {"pookyBlog.common.entity", "pookyBlog.comment.entity","pookyBlog.common.outboxmessage"})
@EnableJpaRepositories(basePackages = {
        "pookyBlog.comment.Repository",
        "pookyBlog.common.outboxmessage"
})
public class CommentApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentApplication.class, args);
    }
}
