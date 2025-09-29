package ar.edu.uade.ecommerce.ventas;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@ConditionalOnProperty(prefix = "ventas.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaVentasConsumerConfig {

    @Bean
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

    @Bean(name = "ventasKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EventMessage> ventasKafkaListenerContainerFactory(
            ConsumerFactory<String, EventMessage> ventasConsumerFactory,
            org.springframework.core.env.Environment env) {
        ConcurrentKafkaListenerContainerFactory<String, EventMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ventasConsumerFactory);
        factory.setConcurrency(env.getProperty("ventas.kafka.concurrency", Integer.class, 3));
        return factory;
    }
}
