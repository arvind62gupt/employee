package com.employee.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoggingController {

    // Inject the raw connection string config value
    @Value("${azure.storage.connection-string:MISSING}")
    private String connectionString;

    // Inject the container name config value
    @Value("${azure.storage.container-name:MISSING}")
    private String containerName;

    @GetMapping("/execute")
    public String doSomething() {
        log.info("This info message is sent automatically to Azure Blob container!");
        log.warn("Warning! Testing the custom Azure log streamer.");

        try {
            int result = 10 / 0;
            log.debug("Result: {}", result);
        } catch (ArithmeticException e) {
            log.error("An error occurred during calculation", e);
        }

        return "Process completed.";
    }

    /**
     * Diagnostic endpoint to verify if secrets are injecting correctly.
     * Route: http://localhost:8080/debug-secrets
     */
    @GetMapping("/debug-secrets")
    public Map<String, Object> debugSecrets() {
        Map<String, Object> status = new HashMap<>();

        // 1. Check if the property is missing entirely
        if ("MISSING".equals(connectionString)) {
            status.put("connectionStringStatus", "ERROR: Property not found in application.properties or environment");
        }
        // 2. Check if it's pointing to an unparsed environment variable placeholder literal like ${AZURE_STORAGE_CONNECTION_STRING}
        else if (connectionString.contains("AZURE_STORAGE_CONNECTION_STRING")) {
            status.put("connectionStringStatus", "ERROR: Variable placeholder found but the actual OS environment variable is empty!");
        }
        // 3. Success (Mask the key for security so it doesn't print on your screen or logs)
        else {
            int maskLength = Math.min(25, connectionString.length());
            status.put("connectionStringStatus", "SUCCESS (Loaded)");
            status.put("connectionStringPreview", connectionString.substring(0, maskLength) + "...");
        }

        status.put("containerName", containerName);
        return status;
    }
}
