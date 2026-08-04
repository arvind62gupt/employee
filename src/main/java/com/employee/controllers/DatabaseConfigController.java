package com.employee.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DriverManagerDataSource; // 👈 Added import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseConfigController {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfigController.class);
    private final DataSource dataSource;

    public DatabaseConfigController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Checks if the application can successfully talk to the Azure Database.
     */
    @GetMapping("/check-connection")
    public ResponseEntity<Map<String, Object>> checkConnection() {
        Map<String, Object> response = new HashMap<>();

        if (dataSource instanceof DriverManagerDataSource) {
            DriverManagerDataSource dmDataSource = (DriverManagerDataSource) dataSource;
            log.info("=== DEBUG DATABASE CREDENTIALS ===");
            log.info("Configured Username: {}", dmDataSource.getUsername());
            log.info("Configured Password from Key Vault: {}", dmDataSource.getPassword());
            log.info("==================================");
        } else {
            log.warn("DataSource is not an instance of DriverManagerDataSource. Cannot extract password plain text.");
        }

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2); // 2-second timeout

            response.put("status", "SUCCESS");
            response.put("connected", isValid);
            response.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Database connection check failed", e);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
