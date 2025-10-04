package ar.edu.uade.ecommerce.ventas;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaConnectivityProbe {
    private static final Logger log = LoggerFactory.getLogger(KafkaConnectivityProbe.class);

    private final Environment env;

    public KafkaConnectivityProbe(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void checkConnectivityOnStartup() {
        // Configurable via properties
        final String bootstrap = env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092");
        final int maxAttempts = env.getProperty("ventas.kafka.connect.maxAttempts", Integer.class, 3);
        final long backoffMs = env.getProperty("ventas.kafka.connect.backoff.ms", Long.class, 2000L);
        final int apiTimeoutMs = env.getProperty("ventas.kafka.connect.apiTimeout.ms", Integer.class, 5000);

        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(apiTimeoutMs));
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, String.valueOf(apiTimeoutMs));

        int attempt = 0;
        Throwable lastError = null;
        while (attempt < maxAttempts) {
            attempt++;
            try (AdminClient admin = AdminClient.create(props)) {
                log.info("[KafkaProbe] Verificando conectividad a Kafka ({}), intento {}/{}", bootstrap, attempt, maxAttempts);
                // describeCluster para forzar conexión
                admin.describeCluster().nodes().get(apiTimeoutMs, TimeUnit.MILLISECONDS);
                log.info("[KafkaProbe] Conectividad OK contra {}", bootstrap);
                return; // listo
            } catch (Exception ex) {
                lastError = ex;
                log.warn("[KafkaProbe] Falló la conexión a Kafka en intento {}/{}: {}", attempt, maxAttempts, ex.getMessage());
                try { Thread.sleep(backoffMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        String msg = String.format("No se pudo conectar con Kafka en %d intentos (bootstrap=%s)", maxAttempts, bootstrap);
        log.error("[KafkaProbe] {}", msg, lastError);
        throw new IllegalStateException(msg, lastError);
    }
}
