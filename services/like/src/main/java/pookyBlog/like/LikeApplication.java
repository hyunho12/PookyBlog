package pookyBlog.like;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"pookyBlog.like", "pookyBlog.common", "pookyBlog.user.Repository", "pookyBlog.post.Repository"}
)
@EntityScan(basePackages = {"pookyBlog.common.entity", "pookyBlog.like.entity","pookyBlog.common.outboxmessage"})
@EnableJpaRepositories(basePackages = {
        "pookyBlog.like.Repository",
        "pookyBlog.common.outboxmessage"
})
public class LikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(LikeApplication.class, args);
    }
}
