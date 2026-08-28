package pookyBlog.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"pookyBlog.post", "pookyBlog.common"}
)
@EntityScan(basePackages = {"pookyBlog.common.Entity", "pookyBlog.post.Entity", "pookyBlog.common.outboxmessage"})
@EnableJpaRepositories(basePackages = {
        "pookyBlog.post.Repository",
        "pookyBlog.common.outboxmessage" 
})
public class PostApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostApplication.class, args);
    }
}
