package com.pookyBlog.pookyBlog.Repository;

import com.pookyBlog.pookyBlog.Entity.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findByPostId(Long postId);
}
