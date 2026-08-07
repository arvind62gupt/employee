package com.employee.controllers;

import com.azure.storage.blob.BlobServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext; // Added import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoggingController {

    private final ApplicationContext context;

    @Value("${azure.storage.container-name:emplogs}")
    private String containerName;

    // Inject the lightweight ApplicationContext instead of the heavy Client bean
    public LoggingController(ApplicationContext context) {
        this.context = context;
    }

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

    @GetMapping("/debug-secrets")
    public Map<String, Object> debugSecrets() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Lazily look up the bean ONLY when someone hits this endpoint URL
            BlobServiceClient blobServiceClient = context.getBean(BlobServiceClient.class);

            String accountUrl = blobServiceClient.getAccountUrl();
            log.info("Programmatic Check Success: Connected out to Azure endpoint: {}", accountUrl);
            status.put("connectionStatus", "SUCCESS (Connected to Azure via programmatic config)");
            status.put("activeStorageEndpoint", accountUrl);

        } catch (Exception e) {
            log.error("Programmatic Check Failed: BlobServiceClient bean instantiation error!", e);
            status.put("connectionStatus", "ERROR: Client bean was not found or failed initialization.");
        }

        status.put("containerName", containerName);
        return status;
    }
}
