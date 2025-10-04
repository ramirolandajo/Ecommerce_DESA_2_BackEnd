package ar.edu.uade.ecommerce.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "consumed_event_log", indexes = {
        @Index(name = "idx_cel_event_id", columnList = "eventId", unique = true),
        @Index(name = "idx_cel_status", columnList = "status")
})
public class ConsumedEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private String eventType;
    private String originModule;

    // Guardamos el timestamp como texto ISO para preservar lo recibido
    private String timestampRaw;

    private String topic;
    private Integer partitionId;
    private Long offsetValue;

    @Lob
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    private ConsumedEventStatus status = ConsumedEventStatus.PENDING;

    private Integer attempts = 0;

    @Lob
    private String lastError;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // ===== ACK al middleware =====
    private Boolean ackSent = false;
    private Integer ackAttempts = 0;

    @Lob
    private String ackLastError;

    private OffsetDateTime ackLastAt;
}
