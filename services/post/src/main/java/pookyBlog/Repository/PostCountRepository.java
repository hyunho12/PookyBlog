package pookyBlog.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pookyBlog.Entity.PostCount;

public interface PostCountRepository extends JpaRepository<PostCount,Long> {
    @Query("select pc.postCount from PostCount pc where pc.id = :postId")
    Long findPostCountById(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("update PostCount pc set pc.postCount = pc.postCount + 1 where pc.id = :postId")
    void increasePostCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("update PostCount pc set pc.postCount = pc.postCount - 1 where pc.id = :postId")
    void decreasePostCount(@Param("postId") Long postId);
}
