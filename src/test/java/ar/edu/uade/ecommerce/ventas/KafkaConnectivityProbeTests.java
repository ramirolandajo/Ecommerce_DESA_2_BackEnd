package ar.edu.uade.ecommerce.ventas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.env.Environment;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaConnectivityProbeTests {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void probe_whenCannotConnect_throwsIllegalState() {
        Environment env = mock(Environment.class);
        when(env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"))
                .thenReturn("invalid:9092");
        KafkaConnectivityProbe probe = new KafkaConnectivityProbe(env);
        // No podemos interceptar AdminClient.create sin mock static, pero el intento de conectar debería
        // fallar con IllegalStateException tras reintentos.
        assertThrows(IllegalStateException.class, probe::probe);
    }
}
