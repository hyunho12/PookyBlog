package pookyBlog.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private final JwtRelayExchangeFilter jwtRelayExchangeFilter;

    public WebClientConfig(JwtRelayExchangeFilter jwtRelayExchangeFilter) {
        this.jwtRelayExchangeFilter = jwtRelayExchangeFilter;
    }

    private WebClient client(String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).filter(jwtRelayExchangeFilter.filter()).build();
    }
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
        return client(commentServiceUrl);
    }

    @Bean
    public WebClient hotPostWebClient(){
        return client(hotPostServiceUrl);
    }

    @Bean
    public WebClient likeWebClient(){
        return client(likeServiceUrl);
    }

    @Bean
    public WebClient postWebClient(){
        return client(postServiceUrl);
    }

    @Bean
    public WebClient postReadWebClient(){
        return client(postReadServiceUrl);
    }

    @Bean
    public WebClient userWebClient(){
        return client(userServiceUrl);
    }

    @Bean
    public WebClient viewWebClient(){
        return client(viewServiceUrl);
    }
}
