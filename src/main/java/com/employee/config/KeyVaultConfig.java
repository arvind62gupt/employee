package com.employee.config;//package com.employee.config;
//
//import com.azure.identity.DefaultAzureCredential;
//import com.azure.identity.DefaultAzureCredentialBuilder;
//import com.azure.security.keyvault.secrets.SecretClient;
//import com.azure.security.keyvault.secrets.SecretClientBuilder;
//import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class KeyVaultConfig {
//
//    private static final Logger log = LoggerFactory.getLogger(KeyVaultConfig.class);
//
//    public void fetchSecretFromAzure() {
//
//        // Fixed: Replaced System.getenv() with a clean, hardcoded string literal
//        String keyVaultUrl = "https://empkey62vault.vault.azure.net/";
//        String secretName = "empsecret1";
//
//        log.info("Initializing Azure Key Vault connection to: {}", keyVaultUrl);
//
//        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
//
//        SecretClient secretClient = new SecretClientBuilder()
//                .vaultUrl(keyVaultUrl)
//                .credential(credential)
//                .buildClient();
//
//        try {
//            KeyVaultSecret secret = secretClient.getSecret(secretName);
//            log.info("Successfully retrieved secret: {}", secretName);
//
//            // If you need to verify it works locally, you can uncomment the line below:
//            // log.info("Secret value is: {}", secret.getValue());
//
//        } catch (Exception e) {
//            log.error("Failed to connect or fetch secret: {}", e.getMessage(), e);
//        }
//    }
//}
