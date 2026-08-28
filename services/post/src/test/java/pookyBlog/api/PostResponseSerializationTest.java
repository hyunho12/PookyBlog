package pookyBlog.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pookyBlog.common.Dto.Request.PostSearch;
import pookyBlog.common.Dto.Response.PostListResponse;
import pookyBlog.common.Dto.Response.PostResponse;
import pookyBlog.post.Service.PostService;
import pookyBlog.post.controller.PostApiController;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostResponseSerializationTest {
    private final PostService postService = mock(PostService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new PostApiController(postService)).build();

    @Test
    void emptyListIsAValidJsonArray() throws Exception {
        when(postService.getListPosts(any(PostSearch.class))).thenReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    void listContainsOnlyScalarListFields() throws Exception {
        when(postService.getListPosts(any(PostSearch.class))).thenReturn(List.of(
                new PostListResponse(1L, "title", "writer", "2026.08.21")));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("title"))
                .andExpect(jsonPath("$[0].createdDate").value("2026.08.21"))
                .andExpect(jsonPath("$[0].view").doesNotExist())
                .andExpect(jsonPath("$[0].comments").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(jsonPath("$[0].posts").doesNotExist());
    }

    @Test
    void detailContainsOnlyScalarDetailFields() throws Exception {
        when(postService.get(1L)).thenReturn(PostResponse.builder()
                .id(1L).title("title").content("content").writer("writer")
                .createdDate("2026.08.21").modifiedDate("2026.08.22").build());

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("content"))
                .andExpect(jsonPath("$.modifiedDate").value("2026.08.22"))
                .andExpect(jsonPath("$.comments").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.posts").doesNotExist());
    }
}
