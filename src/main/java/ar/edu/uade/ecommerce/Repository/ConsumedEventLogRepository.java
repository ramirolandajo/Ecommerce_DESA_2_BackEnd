package ar.edu.uade.ecommerce.Repository;

import ar.edu.uade.ecommerce.Entity.ConsumedEventLog;
import ar.edu.uade.ecommerce.Entity.ConsumedEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConsumedEventLogRepository extends JpaRepository<ConsumedEventLog, Long> {
    Optional<ConsumedEventLog> findByEventId(String eventId);

    List<ConsumedEventLog> findByStatusInAndAttemptsLessThanAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            Collection<ConsumedEventStatus> statuses,
            Integer attempts,
            OffsetDateTime updatedAt,
            Pageable pageable
    );

    // Nuevos helpers para inspección
    List<ConsumedEventLog> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    long countByStatus(ConsumedEventStatus status);
}
