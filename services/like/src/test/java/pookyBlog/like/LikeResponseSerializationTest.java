package pookyBlog.like;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pookyBlog.like.Service.LikeService;
import pookyBlog.like.controller.LikeApiController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LikeResponseSerializationTest {
    private final LikeService likeService = mock(LikeService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new LikeApiController(likeService)).build();

    @Test
    void mutationAndCountResponsesAreScalarOnly() throws Exception {
        when(likeService.getPostLikeCount(1L)).thenReturn(4L);

        mockMvc.perform(post("/likes/1").param("userId", "3"))
                .andExpect(status().isOk()).andExpect(content().string(not(emptyString())));
        mockMvc.perform(delete("/likes/1").param("userId", "3"))
                .andExpect(status().isOk()).andExpect(content().string(not(emptyString())));
        mockMvc.perform(get("/likes/count/1"))
                .andExpect(status().isOk()).andExpect(content().string("4"));
    }
}
