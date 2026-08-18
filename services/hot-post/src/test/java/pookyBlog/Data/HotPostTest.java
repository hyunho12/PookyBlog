package pookyBlog.Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import pookyBlog.Repository.HotPostListRepository;
import pookyBlog.Repository.PostCreatedTimeRepository;
import pookyBlog.common.event.Event;
import pookyBlog.common.event.EventPayload;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.CommentCreatedEventPayload;
import pookyBlog.common.event.payload.PostLikedEventPayload;
import pookyBlog.common.event.payload.PostViewedEventPayload;
import pookyBlog.utils.TimeCalculatorUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class HotPostTest {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private HotPostListRepository hotPostListRepository;

    @Autowired
    private PostCreatedTimeRepository postCreatedTimeRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Long postId = 777L;
    private final String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        postCreatedTimeRepository.createOrUpdate(
                postId,
                LocalDateTime.now(),
                TimeCalculatorUtils.calculatorDurationToMidnight()
        );
    }

    @Test
    void 인기글_생성_카프카_이벤트_전파_후_Redis에_저장된다() throws Exception {
        System.out.println("🔥 Kafka 이벤트 전송 시작");
        // given: 댓글, 좋아요, 조회수 이벤트 전송
        sendKafkaEvent(EventType.COMMENT_CREATED, CommentCreatedEventPayload.builder()
                .postId(postId)
                .postCommentCount(3L)
                .build());

        sendKafkaEvent(EventType.POST_LIKED, PostLikedEventPayload.builder()
                .postId(postId)
                .postLikeCount(5L)
                .postLikeId(1L)
                .build());

        sendKafkaEvent(EventType.POST_VIEWED, PostViewedEventPayload.builder()
                .postId(postId)
                .postViewCount(100L)
                .build());

        // when & then: 인기글에 해당 postId가 포함되어 있는지 기다리며 확인
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Long> hotPosts = hotPostListRepository.getAll(dateStr);
            System.out.println("🔥 현재 인기글 목록: " + hotPosts);
            assertThat(hotPosts).contains(postId);
        });
    }

    private <T extends EventPayload> void sendKafkaEvent(EventType type, T payload) throws Exception {
        Event<EventPayload> event = Event.of(UUID.randomUUID().getMostSignificantBits(), type, payload);
        kafkaTemplate.send(type.getTopics(), postId.toString(), event.toJson()).get();
    }
}
