package pookyBlog;

import org.junit.jupiter.api.Test;
import pookyBlog.Service.eventhandler.PostLikedEventHandler;
import pookyBlog.Service.eventhandler.PostUnlikedEventHandler;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.PostLikedEventPayload;
import pookyBlog.common.event.payload.PostUnlikedEventPayload;

import static org.assertj.core.api.Assertions.assertThat;

class HotPostLikeEventHandlerTest {
    @Test
    @SuppressWarnings("unchecked")
    void likeAndUnlikeScoresUsePostIdInsteadOfLikeRecordId() {
        Long postId = 42L;
        Long likeRecordId = 73L;
        Event<PostLikedEventPayload> liked = (Event<PostLikedEventPayload>) (Event<?>) Event.of(
                1L, EventType.POST_LIKED,
                PostLikedEventPayload.builder().postId(postId).postLikeId(likeRecordId).build());
        Event<PostUnlikedEventPayload> unliked = (Event<PostUnlikedEventPayload>) (Event<?>) Event.of(
                2L, EventType.POST_UNLIKED,
                PostUnlikedEventPayload.builder().postId(postId).postLikeId(likeRecordId).build());

        assertThat(new PostLikedEventHandler(null).findPostId(liked)).isEqualTo(postId);
        assertThat(new PostUnlikedEventHandler(null).findPostId(unliked)).isEqualTo(postId);
    }
}
