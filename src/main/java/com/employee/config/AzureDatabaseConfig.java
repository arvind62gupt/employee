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
import org.springframework.context.annotation.Profile; // 👈 Ensure this is imported
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class AzureDatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureDatabaseConfig.class);

    @Value("${azure.keyvault.url:}")
    private String keyVaultUrl;

    @Value("${azure.keyvault.secret.db-username:}")
    private String usernameSecretName;

    @Value("${azure.keyvault.secret.db-password:}")
    private String passwordSecretName;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    // 1. This bean only runs when the "test" profile is NOT active
    @Bean
    @Profile("!test")
    public DataSource dataSource() {
        log.info("Initializing DataSource using Azure Key Vault secrets from: {}", keyVaultUrl);

        try {
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

            SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUrl)
                    .credential(credential)
                    .buildClient();

            log.info("Fetching database credentials from Key Vault...");
            String dbUsername = secretClient.getSecret(usernameSecretName).getValue();
            String dbPassword = secretClient.getSecret(passwordSecretName).getValue();

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

    // 2. This bean ONLY runs during tests, completely removing Azure dependencies
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        log.info("Test profile active: Initializing local in-memory H2 DataSource.");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
