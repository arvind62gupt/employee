package com.employee.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${azure.keyvault.url:}")
    private String keyVaultUrl;

    // Inside com.employee.config.KafkaConfig.java

    // Point explicitly to your dedicated producer jaas configuration token
    @Value("${azure.keyvault.secret.kafka-producer-jaas:kafka-producer-jaas}")
    private String kafkaSecretName;

    @Bean
    @Profile("!test")
    public ProducerFactory<String, String> producerFactory() {
        log.info("Initializing Production Kafka Producer Factory using direct producer jaas map profile");

        try {
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(credential)
                    .buildClient();

            String jaasConfig = secretClient.getSecret(kafkaSecretName).getValue();

            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "employee-kafka-ns.servicebus.windows.net:9093");
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

            // Inject the complete production string exactly as written in Azure
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig.trim());

            return new DefaultKafkaProducerFactory<>(configProps);
        } catch (Exception e) {
            log.error("Critical Failure: Could not configure Kafka Producer via Key Vault", e);
            throw new IllegalStateException("Failed to initialize Kafka Producer via Key Vault", e);
        }
    }


    @Bean
    @Profile("test")
    public ProducerFactory<String, String> testProducerFactory() {
        log.info("Test profile active: Initializing local mock Kafka Producer Factory.");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
