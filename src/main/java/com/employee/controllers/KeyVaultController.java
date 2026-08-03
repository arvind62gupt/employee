package com.employee.controllers;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secrets")
public class KeyVaultController {

    private static final Logger log = LoggerFactory.getLogger(KeyVaultController.class);

    /**
     * Endpoint to fetch the secret from Azure Key Vault.
     * URL: http://localhost:8080/api/secrets/db-password
     */
    @GetMapping("/db-password")
    public ResponseEntity<String> fetchSecretFromAzure() {

        String keyVaultUrl = "https://empkey62vault.vault.azure.net/";
        String secretName = "spring-datasource-password";//Postmaster@2026

        log.info("REST request received to fetch secret from Key Vault: {}", keyVaultUrl);

        try {
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(credential)
                    .buildClient();

            KeyVaultSecret secret = secretClient.getSecret(secretName);
            log.info("Successfully retrieved secret metadata for: {}", secretName);

            return ResponseEntity.ok(secret.getValue());

        } catch (Exception e) {
            log.error("Failed to connect or fetch secret: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving secret: " + e.getMessage());
        }
    }
}
