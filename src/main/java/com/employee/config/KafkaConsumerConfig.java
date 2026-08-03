package com.employee.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {

        log.info("Initializing Kafka Consumer Factory");

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "employee-kafka-ns.servicebus.windows.net:9093");

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "employee-group");

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        props.put(
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                "SASL_SSL");

        props.put(
                SaslConfigs.SASL_MECHANISM,
                "PLAIN");

        props.put(
                SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"$ConnectionString\" "
                        + "password=\"Endpoint=sb://employee-kafka-ns.servicebus.windows.net/;"
                        + "SharedAccessKeyName=kafka-policy;"
                        + "SharedAccessKey=7TZ8OmeqrdWV7BlW4VjHRIx7BX38XuR+A+AEhNW711g=;"
                        + "EntityPath=employee-events\";"
        );

        log.info("Kafka Consumer configured successfully");
        log.info("Consumer Group: employee-group");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {

        log.info("Creating Kafka Listener Container Factory");

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        log.info("Kafka Listener Container Factory created successfully");

        return factory;
    }
}