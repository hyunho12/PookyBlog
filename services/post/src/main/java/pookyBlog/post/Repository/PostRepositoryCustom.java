package pookyBlog.post.Repository;

import pookyBlog.common.Dto.Request.PostSearch;
import pookyBlog.common.Dto.Response.PostIndexResponse;
import pookyBlog.common.Entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {
    List<Post> getList(PostSearch postSearch);
    Page<PostIndexResponse> getAllListWithPagination(PostSearch postSearch, Pageable pageable);
    List<PostIndexResponse> getAllPostWithPaginationWithoutCount(PostSearch postSearch);
    Optional<Post> findByIdWithComments(Long id);
}
