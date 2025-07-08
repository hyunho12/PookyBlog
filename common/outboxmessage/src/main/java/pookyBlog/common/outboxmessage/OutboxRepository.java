package pookyBlog.common.outboxmessage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    @Query("SELECT o FROM Outbox o WHERE o.shardKey = :shardKey AND o.createdAt <= :from ORDER BY o.createdAt ASC")
    List<Outbox> findOutboxes(@Param("shardKey") Long shardKey,
                              @Param("from") LocalDateTime from,
                              Pageable pageable);
}
