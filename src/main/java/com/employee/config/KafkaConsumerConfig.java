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

    @Value("${azure.keyvault.secret.kafka-password:eventhub-secret}")
    private String kafkaSecretName;

    // 1. This Consumer Factory only runs in non-test profiles using real Key Vault secrets
    @Bean
    @Profile("!test")
    public ConsumerFactory<String, String> consumerFactory() {
        log.info("Initializing Production Kafka Consumer Factory using Key Vault: {}", keyVaultUrl);

        try {
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(credential)
                    .buildClient();

            log.info("Fetching Kafka connection string from Key Vault...");
            String connectionString = secretClient.getSecret(kafkaSecretName).getValue();

            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "employee-kafka-ns.servicebus.windows.net:9093");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "employee-group");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

            // 🎯 THE FIX: String concatenation prevents internal Azure token characters like '%' from crashing the format parser.
            // .trim() strips hidden newline anomalies, and EntityPath is correctly attached outside the core password block.
            String jaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required "
                    + "username=\"$ConnectionString\" "
                    + "password=\"" + connectionString.trim() + "\" "
                    + "EntityPath=\"employee-events\";";

            props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);

            return new DefaultKafkaConsumerFactory<>(props);
        } catch (Exception e) {
            log.error("Critical Failure: Could not configure Kafka Consumer via Key Vault", e);
            throw new IllegalStateException("Failed to initialize Kafka Consumer via Key Vault", e);
        }
    }

    // 2. This Consumer Factory only runs during unit/integration tests
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
        log.info("Creating Kafka Listener Container Factory bound to active profile factory");
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
