package pookyBlog.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pookyBlog.response.PostResponse;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostClient {
    private RestClient restClient;

    @Value("${spring.endpoints.pooky-board-post-service.url}")
    private String postServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(postServiceUrl);
    }

    public Optional<PostResponse> get(Long postId){
        try{
            PostResponse postResponse = restClient.get()
                    .uri("/posts/{postId}", postId)
                    .retrieve()
                    .body(PostResponse.class);
            return Optional.ofNullable(postResponse);
        }
        catch (Exception e){
            log.error("[PostClient.get] postId={}",postId,e);
            return Optional.empty();
        }
    }
}
