package com.employee.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class AzureDatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureDatabaseConfig.class);

    @Value("${azure.keyvault.url}")
    private String keyVaultUrl;

    @Value("${azure.keyvault.secret.db-username}")
    private String usernameSecretName;

    @Value("${azure.keyvault.secret.db-password}")
    private String passwordSecretName;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        log.info("Initializing DataSource using Azure Key Vault secrets from: {}", keyVaultUrl);

        try {
            // Instantiate Azure Credentials
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

            // Build the Key Vault client
            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(credential)
                    .buildClient();

            // Fetch credentials securely at startup
            log.info("Fetching database credentials from Key Vault...");
            String dbUsername = secretClient.getSecret(usernameSecretName).getValue();
            String dbPassword = secretClient.getSecret(passwordSecretName).getValue();

            // Create and populate the DataSource object
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(driverClassName);
            dataSource.setUrl(dbUrl);
            dataSource.setUsername(dbUsername);
            dataSource.setPassword(dbPassword);

            log.info("DataSource successfully configured with Azure Key Vault credentials.");
            return dataSource;

        } catch (Exception e) {
            log.error("Critical Failure: Could not configure DataSource via Key Vault", e);
            throw new IllegalStateException("Failed to initialize database connection via Key Vault", e);
        }
    }
}

