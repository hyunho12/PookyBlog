package pookyBlog.Repository;

import pookyBlog.Dto.Request.PostSearch;
import pookyBlog.Dto.Response.PostIndexResponse;
import pookyBlog.Entity.Post;
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
