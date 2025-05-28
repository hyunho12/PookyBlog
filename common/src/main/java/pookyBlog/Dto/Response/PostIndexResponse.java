package pookyBlog.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PostIndexResponse {
    private Long id;
    private String title;
    private String writer;
    private String createDate;
}
