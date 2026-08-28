package pookyBlog.common.Dto.Response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import pookyBlog.common.Entity.Comment;

@Getter
public class CommentResponse {
    private final Long id;
    private final Long postId;
    private final Long userId;
    private final String nickname;
    private final String comments;
    private final String createdDate;

    public CommentResponse(Comment comment) {
        this(comment.getId(), comment.getPosts().getId(), comment.getUser().getId(),
                comment.getUser().getNickname(), comment.getComments(), comment.getCreatedDate());
    }

    @JsonCreator
    public CommentResponse(@JsonProperty("id") Long id,
                           @JsonProperty("postId") Long postId,
                           @JsonProperty("userId") Long userId,
                           @JsonProperty("nickname") String nickname,
                           @JsonProperty("comments") String comments,
                           @JsonProperty("createdDate") String createdDate) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.nickname = nickname;
        this.comments = comments;
        this.createdDate = createdDate;
    }
}
