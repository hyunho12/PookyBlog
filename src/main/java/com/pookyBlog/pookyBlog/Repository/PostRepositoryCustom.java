package com.pookyBlog.pookyBlog.Repository;

import com.pookyBlog.pookyBlog.Dto.Request.PostSearch;
import com.pookyBlog.pookyBlog.Entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {
    List<Post> getList(PostSearch postSearch);
    Page<Post> getAllListWithPagination(PostSearch postSearch, Pageable pageable);
    Optional<Post> findByIdWithComments(Long id);
}
