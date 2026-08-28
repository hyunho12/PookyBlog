package pookyBlog.common.Dto.Response;

import java.util.Map;

public record PostViewCountsResponse(Map<Long, Long> counts) {
    public PostViewCountsResponse {
        counts = counts == null ? Map.of() : Map.copyOf(counts);
    }
}
