package pookyBlog.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${service.url.comment}")
    private String commentServiceUrl;

    @Value("${service.url.hot-post}")
    private String hotPostServiceUrl;

    @Value("${service.url.like}")
    private String likeServiceUrl;

    @Value("${service.url.post}")
    private String postServiceUrl;

    @Value("${service.url.post-read}")
    private String postReadServiceUrl;

    @Value("${service.url.user}")
    private String userServiceUrl;

    @Value("${service.url.view}")
    private String viewServiceUrl;

    @Bean
    public WebClient commentWebClient(){
        return WebClient.builder().baseUrl(commentServiceUrl).build();
    }

    @Bean
    public WebClient hotPostWebClient(){
        return WebClient.builder().baseUrl(hotPostServiceUrl).build();
    }

    @Bean
    public WebClient likeWebClient(){
        return WebClient.builder().baseUrl(likeServiceUrl).build();
    }

    @Bean
    public WebClient postWebClient(){
        return WebClient.builder().baseUrl(postServiceUrl).build();
    }

    @Bean
    public WebClient postReadWebClient(){
        return WebClient.builder().baseUrl(postReadServiceUrl).build();
    }

    @Bean
    public WebClient userWebClient(){
        return WebClient.builder().baseUrl(userServiceUrl).build();
    }

    @Bean
    public WebClient viewWebClient(){
        return WebClient.builder().baseUrl(viewServiceUrl).build();
    }
}
