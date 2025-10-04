package ar.edu.uade.ecommerce.ventas;

import ar.edu.uade.ecommerce.Entity.ConsumedEventLog;
import ar.edu.uade.ecommerce.Entity.ConsumedEventStatus;
import ar.edu.uade.ecommerce.Repository.ConsumedEventLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/health/consumidores/inventario")
public class VentasConsumersHealthController {

    private final ConsumedEventLogRepository repo;

    public VentasConsumersHealthController(ConsumedEventLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Map<String, Object> status(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        Map<String, Object> out = new HashMap<>();
        out.put("processed", repo.countByStatus(ConsumedEventStatus.PROCESSED));
        out.put("pending", repo.countByStatus(ConsumedEventStatus.PENDING));
        out.put("error", repo.countByStatus(ConsumedEventStatus.ERROR));

        List<ConsumedEventLog> last = repo.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, Math.max(1, Math.min(limit, 100))));
        out.put("last", last.stream().map(e -> Map.of(
                "eventId", e.getEventId(),
                "eventType", e.getEventType(),
                "status", e.getStatus(),
                "attempts", e.getAttempts(),
                "updatedAt", e.getUpdatedAt(),
                "topic", e.getTopic(),
                "partition", e.getPartitionId(),
                "offset", e.getOffsetValue()
        )).toList());
        return out;
    }
}
