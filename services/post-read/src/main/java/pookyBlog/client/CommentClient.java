package pookyBlog.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentClient {
    private RestClient restClient;

    @Value("${spring.endpoints.pooky-board-comment-service.url}")
    private String commentServiceUrl;

    @PostConstruct
    public void initRestClient(){
        restClient = RestClient.create(commentServiceUrl);
    }

    public long count(Long postId){
        try{
            return restClient.get()
                    .uri("/comments/post/{postId}/count",postId)
                    .retrieve()
                    .body(Long.class);
        }
        catch(Exception e){
            log.error("[CommentClient.count] postId={}", postId, e);
            return 0;
        }
    }
}
