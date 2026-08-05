package com.employee.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController; // Added import

@RestController // Added annotation so Spring registers this class as a Web Controller
public class LoggingController {

    private static final Logger log = LoggerFactory.getLogger(LoggingController.class);

    @GetMapping("/execute")
    public String doSomething() {
        log.info("This info message is sent automatically to Azure Blob container!");
        log.warn("Warning! Testing the custom Azure log streamer.");

        try {
            int result = 10 / 0;
        } catch (Exception e) {
            log.error("An error occurred during calculation", e);
        }

        return "Process completed.";
    }
}
