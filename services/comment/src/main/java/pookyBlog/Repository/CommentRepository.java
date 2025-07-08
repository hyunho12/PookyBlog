package pookyBlog.Repository;

import pookyBlog.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    long countByPosts_Id(Long postId);
}
