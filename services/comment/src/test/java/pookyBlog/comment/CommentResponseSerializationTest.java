package pookyBlog.comment;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pookyBlog.comment.Service.CommentService;
import pookyBlog.comment.controller.CommentApiController;
import pookyBlog.common.Dto.Request.CommentCreate;
import pookyBlog.common.Dto.Request.CommentUpdate;
import pookyBlog.common.Dto.Response.CommentResponse;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentResponseSerializationTest {
    private final CommentService commentService = mock(CommentService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CommentApiController(commentService)).build();

    @Test
    void emptyCommentListIsAValidJsonArray() throws Exception {
        when(commentService.getComment(1L)).thenReturn(List.of());
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void commentListContainsOnlyScalarFields() throws Exception {
        when(commentService.getComment(1L)).thenReturn(List.of(
                new CommentResponse(2L, 1L, 3L, "nickname", "comment", "2026.08.21")));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nickname").value("nickname"))
                .andExpect(jsonPath("$[0].comments").value("comment"))
                .andExpect(jsonPath("$[0].postId").value(1))
                .andExpect(jsonPath("$[0].userId").value(3))
                .andExpect(jsonPath("$[0].post").doesNotExist())
                .andExpect(jsonPath("$[0].posts").doesNotExist())
                .andExpect(jsonPath("$[0].user").doesNotExist());
    }

    @Test
    void createUpdateAndDeleteResponsesNeverSerializeEntities() throws Exception {
        when(commentService.create(any(CommentCreate.class))).thenReturn(9L);

        mockMvc.perform(post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"postsId\":1,\"userId\":3,\"comment\":\"comment\"}"))
                .andExpect(status().isOk()).andExpect(content().string("9"));
        mockMvc.perform(put("/api/comments/9").param("userId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"updated\"}"))
                .andExpect(status().isOk()).andExpect(content().string(""));
        mockMvc.perform(delete("/api/comments/9").param("userId", "3"))
                .andExpect(status().isOk()).andExpect(content().string(""));
    }
}
