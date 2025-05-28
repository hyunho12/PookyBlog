package pookyBlog.api;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import pookyBlog.Dto.Response.PostResponse;
import pookyBlog.Entity.PostCount;
import pookyBlog.PostApplication;
import pookyBlog.Repository.PostCountRepository;
import pookyBlog.common.snowflake.Snowflake;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = PostApplication.class)
public class PostApiTest {
    RestClient restClient = RestClient.create("http://localhost:8083");
    @Autowired
    private Snowflake snowflake;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostCountRepository postCountRepository;

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
