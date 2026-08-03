package com.employee.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public ProducerFactory<String, String> producerFactory() {

        log.info("Initializing Kafka Producer Factory");

        Map<String, Object> configProps = new HashMap<>();

        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "employee-kafka-ns.servicebus.windows.net:9093");

        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        configProps.put(
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                "SASL_SSL");

        configProps.put(
                SaslConfigs.SASL_MECHANISM,
                "PLAIN");

        // CORRECTED: The password value string below now safely cuts off right after your SharedAccessKey token hash
        configProps.put(
                SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"$ConnectionString\" "
                        + "password=\"Endpoint=sb://employee-kafka-ns.servicebus.windows.net/;"
                        + "SharedAccessKeyName=kafka-policy;"
                        + "SharedAccessKey=7TZ8OmeqrdWV7BlW4VjHRIx7BX38XuR+A+AEhNW711g=\";"
        );

        log.info("Kafka Producer configuration loaded successfully");
        log.info("Bootstrap Server: {}", "employee-kafka-ns.servicebus.windows.net:9093");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {

        log.info("Creating KafkaTemplate bean");

        return new KafkaTemplate<>(producerFactory());
    }
}
