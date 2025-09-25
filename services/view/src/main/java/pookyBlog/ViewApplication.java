package pookyBlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {"pookyBlog.view", "pookyBlog.common"}
)
@EntityScan(basePackages = {"pookyBlog.common.entity", "pookyBlog.view.entity"})
@EnableJpaRepositories(basePackages = "pookyBlog.view.Repository")
public class ViewApplication {
    public static void main(String[] args) {
        SpringApplication.run(ViewApplication.class, args);
    }
}
