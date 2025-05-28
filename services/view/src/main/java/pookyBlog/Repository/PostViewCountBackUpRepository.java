package pookyBlog.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pookyBlog.Entity.PostViewCount;

@Repository
public interface PostViewCountBackUpRepository extends JpaRepository<PostViewCount, Long> {

    @Query(value = "update post_view_count set view_count = :viewCount " +
            "where id = :postId and view_count < :viewCount", nativeQuery = true)
    @Modifying
    int updateViewCount(@Param("postId") Long postId, @Param("viewCount") Long viewCount);
}
