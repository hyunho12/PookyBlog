package pookyBlog.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import pookyBlog.Dto.Request.PostCreate;
import pookyBlog.Dto.Response.PostResponse;
import pookyBlog.Entity.PostCount;
import pookyBlog.PostApplication;
import pookyBlog.Repository.PostCountRepository;
import pookyBlog.common.outboxmessage.Outbox;
import pookyBlog.common.outboxmessage.OutboxRepository;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostCreatedEventPayload;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(classes = PostApplication.class)
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
    private OutboxRepository outboxRepository;

    @Test
    @DisplayName("게시글생성")
    @Commit
    public void createTest() throws Exception{
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
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Outbox> outboxes = outboxRepository.findAll();
            //assertThat(outboxes.isEmpty()).isFalse();

            Outbox outbox = outboxes.get(0);

            assertThat(outbox.getEventType()).isEqualTo(EventType.POST_CREATED);

            String jsonPayload = outbox.getPayload();

            var innerPayload = objectMapper.readTree(jsonPayload).get("payload");
            PostCreatedEventPayload payload = objectMapper.treeToValue(innerPayload, PostCreatedEventPayload.class);

            assertThat(payload.getTitle()).isEqualTo("test1");
            assertThat(payload.getContent()).isEqualTo("content1");
            assertThat(payload.getWriter()).isEqualTo("potter");

            System.out.println(outbox.getPayload());
        });
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
