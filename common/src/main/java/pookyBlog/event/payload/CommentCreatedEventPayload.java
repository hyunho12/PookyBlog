package pookyBlog.event.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pookyBlog.event.EventPayload;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedEventPayload implements EventPayload {
    private Long commentId;
    private String content;
    private String path;
    private Long postId;
    private String writer;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private Long postCommentCount;
}
