package com.employee.controllers;

import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/kafka")
public class KafkaConfigController {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfigController.class);
    private final ProducerFactory<String, String> producerFactory;

    // Inject the active profile's ProducerFactory bean
    public KafkaConfigController(ProducerFactory<String, String> producerFactory) {
        this.producerFactory = producerFactory;
    }

    /**
     * Inspects the active Kafka Producer configuration and prints credentials to the log.
     */
    @GetMapping("/kafka-secret")
    public ResponseEntity<Map<String, Object>> checkKafkaSecret() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> configProps = producerFactory.getConfigurationProperties();

            if (configProps != null && configProps.containsKey(SaslConfigs.SASL_JAAS_CONFIG)) {
                String rawJaasConfig = (String) configProps.get(SaslConfigs.SASL_JAAS_CONFIG);

                // 🎯 TESTING ONLY: Print the full JAAS configuration containing your Key Vault string
                log.info("=========================================");
                log.info("=== DEBUG KAFKA KEY VAULT CREDENTIALS ===");
                log.info("JAAS Config String: {}", rawJaasConfig);
                log.info("=========================================");

                response.put("status", "SUCCESS");
                response.put("message", "Kafka configuration inspected successfully. Check terminal/server logs.");
                response.put("jaasConfigPayload", rawJaasConfig); // Visible directly in your browser window
                return ResponseEntity.ok(response);
            } else {
                log.warn("JAAS Configuration property missing. Is 'test' profile active?");
                response.put("status", "WARNING");
                response.put("message", "JAAS configuration property was not found in the current environment.");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Failed to inspect active Kafka Configuration properties", e);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
