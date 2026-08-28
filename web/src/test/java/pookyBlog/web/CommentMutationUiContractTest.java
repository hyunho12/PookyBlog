package pookyBlog.web;

import com.samskivert.mustache.Mustache;
import org.junit.jupiter.api.Test;
import pookyBlog.common.Dto.Response.CommentResponse;
import pookyBlog.common.Entity.User;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMutationUiContractTest {
    private static final long COMMENT_ID = 349103684996575232L;
    private static final long USER_ID = 348523324499824640L;

    @Test
    void editAndDeleteButtonsUseCommentResponseIdNotAuthenticatedUserId() throws IOException {
        CommentResponse comment = new CommentResponse(
                COMMENT_ID, 1L, USER_ID, "pooky", "comment", "2026.08.21");
        User authenticatedUser = User.builder()
                .id(USER_ID).username("pooky").nickname("pooky").build();

        String html = compiler().compile(resource("templates/comment/list.mustache"))
                .execute(Map.of("comments", List.of(comment), "user", authenticatedUser, "loggedIn", true));

        assertThat(html)
                .contains("class=\"btn-comment-edit dropdown-item\"")
                .contains("class=\"btn-comment-delete dropdown-item text-danger\"")
                .contains("data-comment-id=\"" + COMMENT_ID + "\"")
                .doesNotContain("data-comment-id=\"" + USER_ID + "\"");
    }

    @Test
    void javascriptBuildsPutAndDeleteUrlsFromCommentIdDataset() throws IOException {
        String javascript = resource("static/js/app.js");
        assertThat(javascript)
                .contains("const id = button.dataset.commentId")
                .contains("this.ajax('PUT', `/api/comments/${id}`")
                .contains("this.ajax('DELETE', `/api/comments/${id}`")
                .doesNotContain("dataset.userId");
    }

    private Mustache.Compiler compiler() {
        return Mustache.compiler().withLoader(name -> {
            var input = getClass().getClassLoader().getResourceAsStream("templates/" + name + ".mustache");
            if (input == null) throw new IOException("Missing partial: " + name);
            return new InputStreamReader(input, StandardCharsets.UTF_8);
        });
    }

    private String resource(String path) throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
