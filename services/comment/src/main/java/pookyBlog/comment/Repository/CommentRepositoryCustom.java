package pookyBlog.comment.Repository;

import pookyBlog.common.Entity.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findByPostId(Long postId);
}
