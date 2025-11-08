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


}
