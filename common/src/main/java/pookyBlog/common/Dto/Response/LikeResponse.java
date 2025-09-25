package pookyBlog.common.Dto.Response;

import lombok.Getter;
import pookyBlog.common.Entity.Post;
import pookyBlog.common.Entity.User;

@Getter
public class LikeResponse {
    private Long id;
    private User user;
    private Post post;
    private String createDate;
}
