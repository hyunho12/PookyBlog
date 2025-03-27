package com.pookyBlog.pookyBlog.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreate {
    private Long postsId;
    private Long userId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String comment;
}
