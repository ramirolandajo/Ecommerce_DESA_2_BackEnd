package ar.edu.uade.ecommerce.ventas;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

@Component("kafkaConnectivityProbe")
@ConditionalOnProperty(
        value = "ventas.kafka.probe.enabled",
        havingValue = "true",
        matchIfMissing = true   // keep current behavior unless you turn it off
)
public class KafkaConnectivityProbe {

    private static final Logger log = LoggerFactory.getLogger(KafkaConnectivityProbe.class);

    private final KafkaAdmin kafkaAdmin;

    public KafkaConnectivityProbe(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @PostConstruct
    public void probe() {
        // Reuse Spring’s fully-merged admin properties (includes SASL/TLS if configured)
        Properties props = new Properties();
        for (Map.Entry<String, Object> e : kafkaAdmin.getConfigurationProperties().entrySet()) {
            props.put(e.getKey(), e.getValue());
        }

        final String bootstrap = String.valueOf(props.getOrDefault("bootstrap.servers", "localhost:9092"));
        final int maxAttempts = 3;
        final long[] backoffMs = new long[]{500L, 1000L, 1500L};

        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (AdminClient admin = AdminClient.create(props)) {
                log.info("[KafkaConnectivityProbe] Intento {}/{} conectando a {}...", attempt, maxAttempts, bootstrap);
                admin.listTopics(new ListTopicsOptions().timeoutMs((int) Duration.ofSeconds(3).toMillis()))
                        .names().get();
                log.info("[KafkaConnectivityProbe] Conexión a Kafka OK en intento {}.", attempt);
                return;
            } catch (Exception ex) {
                last = ex;
                log.warn("[KafkaConnectivityProbe] Falló intento {}: {}", attempt, ex.getMessage());
                if (attempt < maxAttempts) {
                    try { Thread.sleep(backoffMs[Math.min(attempt - 1, backoffMs.length - 1)]); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                }
            }
        }
        String msg = String.format("No se pudo conectar a Kafka en %d intentos (bootstrap=%s). Abortando arranque.",
                maxAttempts, bootstrap);
        log.error("[KafkaConnectivityProbe] {} Causa: {}", msg, last != null ? last.toString() : "desconocida");
        throw new IllegalStateException(msg, last);
    }
}
