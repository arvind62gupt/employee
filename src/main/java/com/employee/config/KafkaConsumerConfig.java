package com.employee.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${azure.keyvault.url:}")
    private String keyVaultUrl;

    // 🎯 POINT TO NEW SECRET NAME
    // Inside com.employee.config.KafkaConsumerConfig.java

    // Point explicitly to your dedicated consumer jaas configuration token
    @Value("${azure.keyvault.secret.kafka-consumer-jaas:kafka-consumer-jaas}")
    private String kafkaSecretName;

    @Bean
    @Profile("!test")
    public ConsumerFactory<String, String> consumerFactory() {
        log.info("Initializing Production Kafka Consumer Factory using dedicated consumer jaas map profile");

        try {
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(credential)
                    .buildClient();

            // Fetches the string that already contains EntityPath format parameters safely embedded inside it
            String jaasConfig = secretClient.getSecret(kafkaSecretName).getValue();

            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "employee-kafka-ns.servicebus.windows.net:9093");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "employee-group");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

            // Inject cleanly with absolutely zero string replace manipulation operations
            props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig.trim());

            return new DefaultKafkaConsumerFactory<>(props);
        } catch (Exception e) {
            log.error("Critical Failure: Could not configure Kafka Consumer via Key Vault", e);
            throw new IllegalStateException("Failed to initialize Kafka Consumer via Key Vault", e);
        }
    }


    @Bean
    @Profile("test")
    public ConsumerFactory<String, String> testConsumerFactory() {
        log.info("Test profile active: Initializing local mock Kafka Consumer Factory.");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "employee-group-test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
