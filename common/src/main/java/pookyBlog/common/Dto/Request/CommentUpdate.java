package pookyBlog.common.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdate {
    @NotBlank(message = "Comment content is required.")
    private String content;
}
