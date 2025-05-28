package pookyBlog.Dto.Response;

import lombok.Getter;
import pookyBlog.Entity.Post;
import pookyBlog.Entity.User;

@Getter
public class LikeResponse {
    private Long id;
    private User user;
    private Post post;
    private String createDate;
}
