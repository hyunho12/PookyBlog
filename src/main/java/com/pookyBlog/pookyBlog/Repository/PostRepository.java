package com.pookyBlog.pookyBlog.Repository;

import com.pookyBlog.pookyBlog.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long>, PostRepositoryCustom{
}
