package pookyBlog.common.Dto.Response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import pookyBlog.common.Entity.Post;

@Getter
public class PostListResponse {
    private final Long id;
    private final String title;
    private final String writer;
    private final String createdDate;

    public PostListResponse(Post post) {
        this(post.getId(), post.getTitle(), post.getWriter(), post.getCreatedDate());
    }

    @JsonCreator
    public PostListResponse(@JsonProperty("id") Long id,
                            @JsonProperty("title") String title,
                            @JsonProperty("writer") String writer,
                            @JsonProperty("createdDate") String createdDate) {
        this.id = id;
        this.title = title;
        this.writer = writer;
        this.createdDate = createdDate;
    }
}
