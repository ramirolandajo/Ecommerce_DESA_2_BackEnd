package ar.edu.uade.ecommerce.ventas;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Properties;

@Component("kafkaConnectivityProbe")
public class KafkaConnectivityProbe {
    private static final Logger log = LoggerFactory.getLogger(KafkaConnectivityProbe.class);

    private final Environment env;

    public KafkaConnectivityProbe(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void probe() {
        String bootstrap = env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092");
        int maxAttempts = 3; // fijo a 3 intentos
        long[] backoffMs = new long[]{500L, 1000L, 1500L};

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrap);
        int attempt = 0;
        Exception last = null;
        while (attempt < maxAttempts) {
            attempt++;
            try (AdminClient admin = AdminClient.create(props)) {
                log.info("[KafkaConnectivityProbe] Intento {}/{} conectando a {}...", attempt, maxAttempts, bootstrap);
                // listTopics con timeout pequeño para no bloquear
                admin.listTopics(new ListTopicsOptions().timeoutMs((int) Duration.ofSeconds(2).toMillis()))
                        .names().get();
                log.info("[KafkaConnectivityProbe] Conexión a Kafka OK en intento {}.", attempt);
                return; // éxito
            } catch (Exception ex) {
                last = ex;
                log.warn("[KafkaConnectivityProbe] Falló intento {}: {}", attempt, ex.getMessage());
                if (attempt < maxAttempts) {
                    try { Thread.sleep(backoffMs[Math.min(attempt - 1, backoffMs.length - 1)]); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        String msg = String.format("No se pudo conectar a Kafka en %d intentos (bootstrap=%s). Abortando arranque.", maxAttempts, bootstrap);
        log.error("[KafkaConnectivityProbe] {} Causa: {}", msg, last != null ? last.toString() : "desconocida");
        throw new IllegalStateException(msg, last);
    }
}
