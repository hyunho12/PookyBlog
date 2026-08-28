package pookyBlog.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;
import pookyBlog.common.jwt.JwtCookieFactory;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@Import(JwtCookieFactory.class)
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
