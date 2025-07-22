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
public class ViewClient {
    private RestClient restClient;

    @Value("${spring.endpoints.pooky-board-view-service.url}")
    private String viewServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(viewServiceUrl);
    }

    public long count(Long postId){
        try{
            return restClient.get()
                    .uri("/{postId}/count",postId)
                    .retrieve()
                    .body(Long.class);
        }
        catch (Exception e){
            log.error("[ViewClient.count] postId",postId,e);
            return 0;
        }
    }
}
