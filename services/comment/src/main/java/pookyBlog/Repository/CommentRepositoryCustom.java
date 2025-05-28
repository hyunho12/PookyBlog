package pookyBlog.Repository;

import pookyBlog.Entity.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findByPostId(Long postId);
}
