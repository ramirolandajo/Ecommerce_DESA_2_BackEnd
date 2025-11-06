package ar.edu.uade.ecommerce.ventas;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaVentasConsumerConfigTests {

    private static Environment mockEnv(Map<String, Object> map) {
        Environment env = mock(Environment.class);
        when(env.getProperty(eq("spring.kafka.bootstrap-servers"), eq("localhost:9092")))
                .thenReturn((String) map.getOrDefault("spring.kafka.bootstrap-servers", "localhost:9092"));
        when(env.getProperty(eq("spring.kafka.consumer.group-id"), eq("ventas-ms")))
                .thenReturn((String) map.getOrDefault("spring.kafka.consumer.group-id", "ventas-ms"));
        when(env.getProperty(eq("spring.kafka.consumer.auto-offset-reset"), eq("earliest")))
                .thenReturn((String) map.getOrDefault("spring.kafka.consumer.auto-offset-reset", "earliest"));
        when(env.getProperty(eq("ventas.kafka.concurrency"), eq(Integer.class), eq(3)))
                .thenReturn((Integer) map.getOrDefault("ventas.kafka.concurrency", 3));
        when(env.getProperty(eq("ventas.kafka.error.backoff.ms"), eq(Long.class), eq(500L)))
                .thenReturn((Long) map.getOrDefault("ventas.kafka.error.backoff.ms", 500L));
        when(env.getProperty(eq("ventas.kafka.error.maxAttempts"), eq(Integer.class), eq(3)))
                .thenReturn((Integer) map.getOrDefault("ventas.kafka.error.maxAttempts", 3));
        when(env.getProperty(eq("ventas.kafka.dlq.enabled"), eq(Boolean.class), eq(false)))
                .thenReturn((Boolean) map.getOrDefault("ventas.kafka.dlq.enabled", false));
        when(env.getProperty(eq("ventas.kafka.dlq.topicSuffix"), eq(".dlq")))
                .thenReturn((String) map.getOrDefault("ventas.kafka.dlq.topicSuffix", ".dlq"));
        return env;
    }

    @Test
    void ventasConsumerFactory_buildsWithJsonDeserializer() {
        KafkaVentasConsumerConfig cfg = new KafkaVentasConsumerConfig();
        Environment env = mockEnv(Map.of());
        ConsumerFactory<String, EventMessage> cf = cfg.ventasConsumerFactory(env);
        assertNotNull(cf);
        assertInstanceOf(DefaultKafkaConsumerFactory.class, cf);
        @SuppressWarnings("unchecked")
        Map<String, Object> props = ((DefaultKafkaConsumerFactory<String, EventMessage>) cf).getConfigurationProperties();
        assertEquals(StringDeserializer.class, props.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    void ventasKafkaListenerContainerFactory_withoutDLQ_buildsSuccessfully() {
        KafkaVentasConsumerConfig cfg = new KafkaVentasConsumerConfig();
        Environment env = mockEnv(Map.of(
                "ventas.kafka.concurrency", 5,
                "ventas.kafka.error.backoff.ms", 200L,
                "ventas.kafka.error.maxAttempts", 4,
                "ventas.kafka.dlq.enabled", false
        ));
        @SuppressWarnings("unchecked") ConsumerFactory<String, EventMessage> cf = mock(ConsumerFactory.class);
        @SuppressWarnings("unchecked") ObjectProvider<KafkaTemplate<String, Object>> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        ConcurrentKafkaListenerContainerFactory<String, EventMessage> factory =
                cfg.ventasKafkaListenerContainerFactory(cf, env, provider);
        assertNotNull(factory);
        // Se invocó el provider aunque DLQ esté deshabilitado para evaluar disponibilidad
        verify(provider, times(1)).getIfAvailable();
    }

    @Test
    void ventasKafkaListenerContainerFactory_withDLQ_usesTemplateWhenAvailable() {
        KafkaVentasConsumerConfig cfg = new KafkaVentasConsumerConfig();
        Environment env = mockEnv(Map.of(
                "ventas.kafka.concurrency", 2,
                "ventas.kafka.dlq.enabled", true,
                "ventas.kafka.dlq.topicSuffix", ".dead"
        ));
        @SuppressWarnings("unchecked") ConsumerFactory<String, EventMessage> cf = mock(ConsumerFactory.class);
        @SuppressWarnings("unchecked") KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        @SuppressWarnings("unchecked") ObjectProvider<KafkaTemplate<String, Object>> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(template);

        ConcurrentKafkaListenerContainerFactory<String, EventMessage> factory =
                cfg.ventasKafkaListenerContainerFactory(cf, env, provider);
        assertNotNull(factory);
        verify(provider, times(1)).getIfAvailable();
        // No hay excepción al construir con DLQ habilitado y template disponible
    }
}
