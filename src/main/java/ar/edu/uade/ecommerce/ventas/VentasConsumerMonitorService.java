package ar.edu.uade.ecommerce.ventas;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Service
public class VentasConsumerMonitorService {
    private final Map<String, LongAdder> handledByType = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> errorsByType = new ConcurrentHashMap<>();
    private final LongAdder duplicates = new LongAdder();
    private final Deque<String> lastEventIds = new ArrayDeque<>();
    private final int maxLastIds = 100;
    private final Instant start = Instant.now();

    public void recordProcessed(String eventType, String eventId) {
        handledByType.computeIfAbsent(eventType == null ? "<null>" : eventType, k -> new LongAdder()).increment();
        pushEventId(eventId);
    }

    public void recordError(String eventType, String eventId) {
        errorsByType.computeIfAbsent(eventType == null ? "<null>" : eventType, k -> new LongAdder()).increment();
        pushEventId(eventId);
    }

    public void recordDuplicate(String eventId) {
        duplicates.increment();
        pushEventId(eventId);
    }

    private synchronized void pushEventId(String eventId) {
        if (eventId == null) return;
        if (lastEventIds.size() >= maxLastIds) {
            lastEventIds.removeFirst();
        }
        lastEventIds.addLast(eventId);
    }

    public Snapshot snapshot() {
        Snapshot s = new Snapshot();
        s.handledByType = handledByType.entrySet().stream()
                .collect(ConcurrentHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().sum()), ConcurrentHashMap::putAll);
        s.errorsByType = errorsByType.entrySet().stream()
                .collect(ConcurrentHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().sum()), ConcurrentHashMap::putAll);
        s.duplicates = duplicates.sum();
        synchronized (this) {
            s.lastEventIds = lastEventIds.stream().toList();
        }
        s.uptimeMs = Instant.now().toEpochMilli() - start.toEpochMilli();
        return s;
    }

    public static class Snapshot {
        public Map<String, Long> handledByType;
        public Map<String, Long> errorsByType;
        public long duplicates;
        public java.util.List<String> lastEventIds;
        public long uptimeMs;
    }
}

