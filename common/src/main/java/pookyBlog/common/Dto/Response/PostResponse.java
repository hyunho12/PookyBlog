package pookyBlog.common.Dto.Response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pookyBlog.common.Entity.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String writer;
    private final String createdDate;
    private final String modifiedDate;

    public PostResponse(Post post){
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.writer = post.getWriter();
        this.createdDate = post.getCreatedDate();
        this.modifiedDate = post.getModifiedDate();
    }

    @Builder
    @JsonCreator
    public PostResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("writer") String writer,
            @JsonProperty("createdDate") String createdDate,
            @JsonProperty("modifiedDate") String modifiedDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
}
