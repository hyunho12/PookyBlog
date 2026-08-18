package pookyBlog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import pookyBlog.common.Dto.Request.PostCreate;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.PostCreatedEventPayload;
import pookyBlog.common.outboxmessage.Outbox;
import pookyBlog.common.outboxmessage.OutboxEvent;
import pookyBlog.common.outboxmessage.OutboxRepository;
import pookyBlog.post.PostApplication;
import pookyBlog.post.Repository.PostCountRepository;
import pookyBlog.post.Repository.PostRepository;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PostApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@RecordApplicationEvents
class PostKafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCountRepository postCountRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @DisplayName("Kafka 전송 실패 시 POST_CREATED 이벤트를 Outbox에 보존한다")
    void createPost_persistsOutboxWhenKafkaPublishFails() throws Exception {
        PostCreate request = PostCreate.builder()
                .title("t")
                .content("c")
                .writer("w")
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("test broker unavailable")));

        Outbox outbox = null;
        Long postId = null;
        try {
            mockMvc.perform(post("/posts/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            Long outboxId = applicationEvents.stream(OutboxEvent.class)
                    .map(OutboxEvent::getOutbox)
                    .filter(candidate -> candidate.getEventType() == EventType.POST_CREATED)
                    .map(Outbox::getOutboxId)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("POST_CREATED outbox event was not published"));
            outbox = outboxRepository.findById(outboxId)
                    .orElseThrow(() -> new AssertionError("POST_CREATED outbox was not persisted"));

            var eventJson = objectMapper.readTree(outbox.getPayload());
            PostCreatedEventPayload payload = objectMapper.treeToValue(
                    eventJson.get("payload"), PostCreatedEventPayload.class
            );
            postId = payload.getPostId();

            assertThat(outbox.getEventType()).isEqualTo(EventType.POST_CREATED);
            assertThat(payload.getTitle()).isEqualTo(request.getTitle());
            assertThat(payload.getContent()).isEqualTo(request.getContent());
            assertThat(payload.getWriter()).isEqualTo(request.getWriter());
            assertThat(postRepository.findById(postId)).isPresent();
        } finally {
            if (outbox != null) {
                outboxRepository.deleteById(outbox.getOutboxId());
            }
            if (postId != null) {
                postCountRepository.deleteById(postId);
                postRepository.deleteById(postId);
            }
        }
    }
}
