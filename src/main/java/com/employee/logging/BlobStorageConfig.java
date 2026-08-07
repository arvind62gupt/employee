package com.employee.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.employee.logging.AzureBlobAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BlobStorageConfig {

    @Value("${azure.keyvault.url}")
    private String keyVaultUrl;

    @Value("${azure.storage.connection-string-secret-name:azure-storage-connection-string}")
    private String connectionStringSecretName;

    @Value("${azure.storage.container-name:emplogs}")
    private String containerName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        log.info("Initializing BlobServiceClient using Azure Key Vault secrets from: {}", keyVaultUrl);

        try {
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(credential)
                    .buildClient();

            log.info("Fetching Azure Storage connection string from Key Vault...");
            String resolvedConnectionString = secretClient.getSecret(connectionStringSecretName).getValue();

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(resolvedConnectionString)
                    .buildClient();

            // Pass the secrets straight down to Logback right here during compilation creation
            AzureBlobAppender.initializeWithProgrammaticSecrets(resolvedConnectionString, containerName);

            log.info("BlobServiceClient successfully configured with Key Vault connection string.");
            return blobServiceClient;

        } catch (Exception e) {
            log.error("Critical Failure: Could not configure BlobServiceClient via Key Vault", e);
            throw new IllegalStateException("Failed to initialize storage connection via Key Vault", e);
        }
    }
}
