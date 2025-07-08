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
public class PostLikedEventPayload implements EventPayload {
    private Long postLikeId;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
    private Long postLikeCount;
}
