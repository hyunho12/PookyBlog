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
public class LikeClient {
    private RestClient restClient;

    @Value("${spring.endpoints.pooky-board-like-service.url}")
    private String likeServiceUrl;

    @PostConstruct
    public void initRestClient(){
        restClient = RestClient.create(likeServiceUrl);
    }

    public long count(Long postId){
        try{
            return restClient.get()
                    .uri("/count/{postId}",postId)
                    .retrieve()
                    .body(Long.class);
        }
        catch(Exception e){
            log.error("[LikeClient.count] postId={}", postId, e);
            return 0L;
        }
    }
}
