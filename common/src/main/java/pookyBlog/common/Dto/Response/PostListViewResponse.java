package pookyBlog.common.Dto.Response;

import lombok.Getter;

@Getter
public class PostListViewResponse {
    private final Long id;
    private final String title;
    private final String writer;
    private final String createdDate;
    private final Long viewCount;

    public PostListViewResponse(PostListResponse post, Long viewCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.writer = post.getWriter();
        this.createdDate = post.getCreatedDate();
        this.viewCount = viewCount == null ? 0L : viewCount;
    }
}
