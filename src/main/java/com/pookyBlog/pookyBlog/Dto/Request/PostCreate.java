package com.pookyBlog.pookyBlog.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PostCreate {

    @NotBlank(message = "제목을 입력하세요")
    private String title;

    @NotBlank(message = "내용을 입력하세요")
    private String content;

    private String writer;

    @Builder
    public PostCreate(String title, String content, String writer) {
        this.title = title;
        this.content = content;
        this.writer = writer;
    }
}
