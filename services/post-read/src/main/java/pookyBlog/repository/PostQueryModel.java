package pookyBlog.repository;

import lombok.Getter;
import pookyBlog.event.payload.*;
import pookyBlog.response.PostResponse;

import java.time.LocalDateTime;

@Getter
public class PostQueryModel {
    private Long postId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long postCommentCount;
    private Long postLikeCount;

    public static PostQueryModel create(PostCreatedEventPayload payload){
        PostQueryModel postQueryModel = new PostQueryModel();
        postQueryModel.postId = payload.getPostId();
        postQueryModel.title = payload.getTitle();
        postQueryModel.content = payload.getContent();
        postQueryModel.writer = payload.getWriter();
        postQueryModel.createdAt = payload.getCreatedAt();
        postQueryModel.updatedAt = payload.getUpdatedAt();
        postQueryModel.postCommentCount = 0L;
        postQueryModel.postLikeCount = 0L;
        return postQueryModel;
    }

    public static PostQueryModel create(PostResponse post, Long commentCount, Long likeCount){
        PostQueryModel postQueryModel = new PostQueryModel();
        postQueryModel.postId = post.getPostId();
        postQueryModel.title = post.getTitle();
        postQueryModel.content = post.getContent();
        postQueryModel.writer = post.getWriter();
        postQueryModel.createdAt = post.getCreateAt();
        postQueryModel.updatedAt = post.getUpdateAt();
        postQueryModel.postCommentCount = commentCount;
        postQueryModel.postLikeCount = likeCount;
        return postQueryModel;
    }

    public void update(PostUpdatedEventPayload payload){
        this.title = payload.getTitle();
        this.content = payload.getContent();
        this.createdAt = payload.getCreatedAt();
        this.updatedAt = payload.getUpdatedAt();
    }

    public void update(CommentCreatedEventPayload payload){
        this.postCommentCount = payload.getPostCommentCount();
    }

    public void update(CommentDeletedEventPayload payload){
        this.postCommentCount = payload.getPostCommentCount();
    }

    public void update(PostLikedEventPayload payload){
        this.postLikeCount = payload.getPostLikeCount();
    }

    public void update(PostUnlikedEventPayload payload){
        this.postLikeCount = payload.getPostLikeCount();
    }
}
