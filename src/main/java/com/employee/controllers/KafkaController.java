package com.employee.controllers;

import com.employee.services.EmployeeProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaController.class);

    private final EmployeeProducer producer;

    public KafkaController(EmployeeProducer producer) {
        this.producer = producer;
    }

    @GetMapping("/publish")
    public String publish(@RequestParam String message) {

        try {
            log.info("Received request to publish message: {}", message);

            producer.sendMessage(message);

            log.info("Message published successfully: {}", message);

            return "Message Sent Successfully";

        } catch (Exception e) {

            log.error("Failed to publish message: {}", message, e);

            return "Failed to Send Message";
        }
    }
}