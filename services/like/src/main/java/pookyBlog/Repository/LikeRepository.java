package pookyBlog.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pookyBlog.Entity.Like;
import pookyBlog.Entity.LikeCount;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    long countByPostId(Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lc from LikeCount lc where lc.id = :postId")
    Optional<LikeCount> findByIdForUpdate(@Param("postId") Long postId);
    Optional<LikeCount> findByPostId(Long postId);

    @Modifying
    @Query("update LikeCount lc set lc.likeCount = lc.likeCount + 1 where lc.id = :postId")
    void increaseLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("update LikeCount lc set lc.likeCount = lc.likeCount - 1 where lc.id = :postId")
    void decreaseLikeCount(@Param("postId") Long postId);
}
