package pookyBlog.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.common.Dto.Request.PostCreate;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.common.outboxmessage.Outbox;
import pookyBlog.common.outboxmessage.OutboxEvent;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.PostCreatedEventPayload;
import pookyBlog.post.Entity.PostCount;
import pookyBlog.post.PostApplication;
import pookyBlog.post.Repository.PostCountRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(classes = PostApplication.class)
@RecordApplicationEvents
public class PostApiTest {
    @Autowired
    private Snowflake snowflake;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostCountRepository postCountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    @DisplayName("게시글 생성 API가 POST_CREATED 이벤트와 payload를 발행한다")
    public void createPost_publishesPostCreatedEventWithPayload() throws Exception{
        //given
        PostCreate postCreate = PostCreate.builder()
                .title("test1")
                .content("content1")
                .writer("potter")
                .build();
        //when
        mockMvc.perform(
                post("/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postCreate))
        ).andExpect(status().isOk());

        //then
        Outbox outbox = applicationEvents.stream(OutboxEvent.class)
                .map(OutboxEvent::getOutbox)
                .filter(event -> event.getEventType() == EventType.POST_CREATED)
                .findFirst()
                .orElseThrow(() -> new AssertionError("POST_CREATED outbox event was not published"));

        assertThat(outbox.getEventType()).isEqualTo(EventType.POST_CREATED);

        String jsonPayload = outbox.getPayload();

        var innerPayload = objectMapper.readTree(jsonPayload).get("payload");
        PostCreatedEventPayload payload = objectMapper.treeToValue(innerPayload, PostCreatedEventPayload.class);

        assertThat(payload.getTitle()).isEqualTo("test1");
        assertThat(payload.getContent()).isEqualTo("content1");
        assertThat(payload.getWriter()).isEqualTo("potter");
    }

    @Test
    void countTest() throws Exception{
        PostResponse postResponse = PostResponse.builder()
                .id(snowflake.nextId())
                .title("test")
                .content("content")
                .writer("harry")
                .build();

        postCountRepository.save(new PostCount(postResponse.getId(), 0L));

        postCountRepository.increasePostCount(postResponse.getId());
        postCountRepository.increasePostCount(postResponse.getId());
        postCountRepository.flush();

        Optional<PostCount> count = postCountRepository.findById(postResponse.getId());
        assertThat(count).isPresent();
        assertThat(count.get().getPostCount()).isEqualTo(2L);


        mockMvc.perform(get("/posts/count/{postId}", postResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }
}
