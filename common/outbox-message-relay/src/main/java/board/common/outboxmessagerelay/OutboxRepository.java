package board.common.outboxmessagerelay;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    /* SELECT * FROM outbox_table
       WHERE shard_key = ? AND created_at <= ?
       ORDER BY created_at ASC;
    */
    List<Outbox> findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
            Long shardKey,
            LocalDateTime from,
            Pageable pageable
    );
}
