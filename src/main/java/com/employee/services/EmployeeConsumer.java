package com.employee.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmployeeConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeConsumer.class);

    private final EmailService emailService;

    public EmployeeConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(
            topics = "employee-events",
            groupId = "employee-group")
    public void consume(String message) {

        try {

            log.info("Kafka message received: {}", message);

            String[] data = message.split("\\|");

            String action = data[0];
            String email = data[1];

            if ("ADD".equals(action)) {

                emailService.sendNotification(
                        email,
                        "Employee Added",
                        "Employee record added successfully."
                );

            } else if ("UPDATE".equals(action)) {

                emailService.sendNotification(
                        email,
                        "Employee Updated",
                        "Employee record updated successfully."
                );

            } else if ("DELETE".equals(action)) {

                emailService.sendNotification(
                        email,
                        "Employee Deleted",
                        "Employee record deleted successfully."
                );
            }

            log.info("Notification sent successfully to {}", email);

        } catch (Exception e) {

            log.error("Error while processing Kafka message", e);
        }
    }
}