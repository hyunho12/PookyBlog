package pookyBlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = {
                "pookyBlog.client",
                "pookyBlog.consumer",
                "pookyBlog.post",
                "pookyBlog.repository",
                "pookyBlog.service"
        },
        exclude = {
                DataSourceAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
public class PostReadApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostReadApplication.class, args);
    }
}
