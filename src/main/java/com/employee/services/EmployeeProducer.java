package com.employee.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProducer {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public EmployeeProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {

        kafkaTemplate.send("employee-events", message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka message sent successfully: {}", message);
                    } else {
                        log.error("Failed to send Kafka message: {}", message, ex);
                    }
                });
    }

    @PostConstruct
    public void init() {

        log.info("EmployeeProducer Loaded");

        if (kafkaTemplate != null) {
            log.info("KafkaTemplate initialized successfully");
        } else {
            log.error("KafkaTemplate is NULL");
        }
    }
}