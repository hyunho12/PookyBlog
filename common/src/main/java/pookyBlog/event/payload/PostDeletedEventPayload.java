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
public class PostDeletedEventPayload implements EventPayload {
    private Long postId;
    private String title;
    private String content;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long boardPostCount;
}
