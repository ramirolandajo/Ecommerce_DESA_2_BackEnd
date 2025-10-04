package ar.edu.uade.ecommerce.ventas;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@ConditionalOnProperty(prefix = "ventas.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaVentasConsumerConfig {

    @Bean
    @DependsOn("kafkaConnectivityProbe")
    public ConsumerFactory<String, EventMessage> ventasConsumerFactory(org.springframework.core.env.Environment env) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, env.getProperty("spring.kafka.consumer.group-id", "ventas-ms"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, env.getProperty("spring.kafka.consumer.auto-offset-reset", "earliest"));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Config del JsonDeserializer
        JsonDeserializer<EventMessage> deserializer = new JsonDeserializer<>(EventMessage.class, false);
        deserializer.addTrustedPackages("*");
        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    // Producer para DLQ opcional
    @Bean
    @DependsOn("kafkaConnectivityProbe")
    @ConditionalOnProperty(prefix = "ventas.kafka.dlq", name = "enabled", havingValue = "true")
    public ProducerFactory<String, Object> ventasProducerFactory(org.springframework.core.env.Environment env) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @DependsOn("kafkaConnectivityProbe")
    @ConditionalOnProperty(prefix = "ventas.kafka.dlq", name = "enabled", havingValue = "true")
    public KafkaTemplate<String, Object> ventasKafkaTemplate(ProducerFactory<String, Object> ventasProducerFactory) {
        return new KafkaTemplate<>(ventasProducerFactory);
    }

    @Bean(name = "ventasKafkaListenerContainerFactory")
    @DependsOn("kafkaConnectivityProbe")
    public ConcurrentKafkaListenerContainerFactory<String, EventMessage> ventasKafkaListenerContainerFactory(
            ConsumerFactory<String, EventMessage> ventasConsumerFactory,
            org.springframework.core.env.Environment env,
            org.springframework.beans.factory.ObjectProvider<KafkaTemplate<String, Object>> templateProvider) {
        ConcurrentKafkaListenerContainerFactory<String, EventMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ventasConsumerFactory);
        factory.setConcurrency(env.getProperty("ventas.kafka.concurrency", Integer.class, 3));

        // Manejo de errores con reintentos controlados y DLQ opcional
        long backoffMs = env.getProperty("ventas.kafka.error.backoff.ms", Long.class, 500L);
        int maxAttempts = env.getProperty("ventas.kafka.error.maxAttempts", Integer.class, 3);
        FixedBackOff backOff = new FixedBackOff(backoffMs, maxAttempts - 1L);

        KafkaTemplate<String, Object> template = templateProvider.getIfAvailable();
        DefaultErrorHandler errorHandler;
        if (template != null && env.getProperty("ventas.kafka.dlq.enabled", Boolean.class, false)) {
            String topicSuffix = env.getProperty("ventas.kafka.dlq.topicSuffix", ".dlq");
            errorHandler = new DefaultErrorHandler((rec, ex) -> {
                String dlqTopic = rec.topic() + topicSuffix;
                String key = rec.key() != null ? rec.key().toString() : null;
                Object value = rec.value();
                template.send(dlqTopic, key, value);
            }, backOff);
        } else {
            errorHandler = new DefaultErrorHandler(backOff);
        }
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
