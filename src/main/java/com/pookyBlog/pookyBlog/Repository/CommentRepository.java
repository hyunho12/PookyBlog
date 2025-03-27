package com.pookyBlog.pookyBlog.Repository;

import com.pookyBlog.pookyBlog.Entity.Comment;
import com.pookyBlog.pookyBlog.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
}
