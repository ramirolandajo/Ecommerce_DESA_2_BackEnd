package ar.edu.uade.ecommerce.ventas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventIdempotencyService {
    private static final Logger log = LoggerFactory.getLogger(EventIdempotencyService.class);

    private final Map<String, Instant> processed = new ConcurrentHashMap<>();
    private final Duration ttl = Duration.ofHours(24);
    private final int maxSize = 10_000;

    public boolean alreadyProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) return false;
        purgeIfNeeded();
        Instant ts = processed.get(eventId);
        if (ts == null) return false;
        boolean expired = ts.isBefore(Instant.now().minus(ttl));
        if (expired) {
            processed.remove(eventId);
            return false;
        }
        return true;
    }

    public void markProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) return;
        purgeIfNeeded();
        processed.put(eventId, Instant.now());
    }

    private void purgeIfNeeded() {
        if (processed.size() <= maxSize) return;
        // Purga simple: eliminar entradas expiradas primero
        Instant threshold = Instant.now().minus(ttl);
        processed.entrySet().removeIf(e -> e.getValue().isBefore(threshold));
        // Si aún excede, recorta 10% arbitrariamente
        int size = processed.size();
        if (size > maxSize) {
            int toRemove = (int) (size * 0.1);
            processed.keySet().stream().limit(toRemove).forEach(processed::remove);
            log.warn("[Idempotency] Recorte de mapa de eventIds: {} removidos", toRemove);
        }
    }
}

