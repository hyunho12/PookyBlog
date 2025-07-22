package pookyBlog.response;

import lombok.Getter;
import pookyBlog.repository.PostQueryModel;

import java.time.LocalDateTime;

@Getter
public class PostReadResponse {
    private Long postId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Long postCommentCount;
    private Long postLikeCount;
    private Long postViewCount;

    public static PostReadResponse from(PostQueryModel postQueryModel, Long viewCount){
        PostReadResponse response = new PostReadResponse();
        response.postId = postQueryModel.getPostId();
        response.title = postQueryModel.getTitle();
        response.content = postQueryModel.getContent();
        response.writer = postQueryModel.getWriter();
        response.createAt = postQueryModel.getCreatedAt();
        response.updateAt = postQueryModel.getUpdatedAt();
        response.postCommentCount = postQueryModel.getPostCommentCount();
        response.postLikeCount = postQueryModel.getPostLikeCount();
        response.postViewCount = viewCount;
        return response;
    }
}
