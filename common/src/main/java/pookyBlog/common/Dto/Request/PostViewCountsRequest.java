package pookyBlog.common.Dto.Request;

import java.util.List;

public record PostViewCountsRequest(List<Long> postIds) {
    public PostViewCountsRequest {
        postIds = postIds == null ? List.of() : List.copyOf(postIds);
    }
}
