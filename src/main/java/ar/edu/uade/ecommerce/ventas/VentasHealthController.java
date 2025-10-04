package ar.edu.uade.ecommerce.ventas;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health/consumidores")
public class VentasHealthController {

    private final VentasConsumerMonitorService monitor;
    private final Environment env;

    public VentasHealthController(VentasConsumerMonitorService monitor, Environment env) {
        this.monitor = monitor;
        this.env = env;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> out = new HashMap<>();
        VentasConsumerMonitorService.Snapshot s = monitor.snapshot();
        out.put("handledByType", s.handledByType);
        out.put("errorsByType", s.errorsByType);
        out.put("duplicates", s.duplicates);
        out.put("lastEventIds", s.lastEventIds);
        out.put("uptimeMs", s.uptimeMs);

        Map<String, Object> cfg = new HashMap<>();
        cfg.put("bootstrapServers", env.getProperty("spring.kafka.bootstrap-servers"));
        cfg.put("groupId", env.getProperty("spring.kafka.consumer.group-id"));
        cfg.put("topic", env.getProperty("ventas.kafka.topic"));
        cfg.put("concurrency", env.getProperty("ventas.kafka.concurrency"));
        cfg.put("dlqEnabled", env.getProperty("ventas.kafka.dlq.enabled", "false"));
        cfg.put("dlqTopicSuffix", env.getProperty("ventas.kafka.dlq.topicSuffix", ".dlq"));
        cfg.put("errorMaxAttempts", env.getProperty("ventas.kafka.error.maxAttempts", "3"));
        cfg.put("errorBackoffMs", env.getProperty("ventas.kafka.error.backoff.ms", "500"));
        out.put("config", cfg);

        return out;
    }
}

