package pookyBlog.event.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pookyBlog.event.EventPayload;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostViewedEventPayload implements EventPayload {
    private Long postId;
    private Long postViewCount;
}
