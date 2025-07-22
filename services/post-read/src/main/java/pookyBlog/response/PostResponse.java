package pookyBlog.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {
    private Long postId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
