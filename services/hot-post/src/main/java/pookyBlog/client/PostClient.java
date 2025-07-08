package pookyBlog.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostClient {
    private RestClient restClient;

    @Value("${spring.endpoints.pooky-board-post-service.url}")
    private String postServiceUrl;

    @PostConstruct
    void initRestClient(){
        restClient = RestClient.create(postServiceUrl);
    }

    public PostResponse getPost(Long postId){
        try{
            return restClient.get()
                    .uri("/posts/{postId}", postId)
                    .retrieve()
                    .body(PostResponse.class);
        } catch (Exception e){
            log.error("[PostClient.read] postId={}",postId, e);
        }
        return null;
    }

    @Getter
    public static class PostResponse{
        private Long postId;
        private String title;
        private LocalDateTime createAt;
    }
}
