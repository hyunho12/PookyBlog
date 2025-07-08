package pookyBlog.Service.response;

import lombok.Getter;
import lombok.ToString;
import pookyBlog.client.PostClient;

import java.time.LocalDateTime;

@Getter
@ToString
public class HotPostResponse {
    private Long postId;
    private String title;
    private LocalDateTime createAt;

    public static HotPostResponse from(PostClient.PostResponse postResponse){
        HotPostResponse response = new HotPostResponse();
        response.postId = postResponse.getPostId();
        response.title = postResponse.getTitle();
        response.createAt = postResponse.getCreateAt();
        return response;
    }
}
