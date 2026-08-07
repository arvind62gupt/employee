package com.employee.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.AppendBlobClient;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class AzureBlobAppender<E extends DeferredProcessingAware> extends AppenderBase<E> {

    private static String connectionString;
    private static String containerName;
    private String blobPrefix = "springboot-app-";

    private AppendBlobClient appendBlobClient;
    private LocalDate currentLogDate;
    private boolean isInitialized = false;

    /**
     * Statically called by our new BlobStorageConfig class once Key Vault secrets arrive
     */
    public static void initializeWithProgrammaticSecrets(String connStr, String container) {
        connectionString = connStr;
        containerName = container;
    }

    @Override
    public void start() {
        super.start();
    }

    private synchronized boolean lazyInitializeAzureClient() {
        if (isInitialized) return true;
        // Wait until your BlobStorageConfig code injects the downloaded secrets
        if (connectionString == null || containerName == null) return false;

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }

            initializeBlobClient(containerClient);
            this.isInitialized = true;
            return true;
        } catch (Exception e) {
            addError("Failed to lazily establish Azure Blob Client using programmatic secrets", e);
            return false;
        }
    }

    private void initializeBlobClient(BlobContainerClient containerClient) {
        currentLogDate = LocalDate.now();
        String blobName = blobPrefix + currentLogDate + ".log";
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        appendBlobClient = blobClient.getAppendBlobClient();

        if (!appendBlobClient.exists()) {
            appendBlobClient.create();
        }
    }

    @Override
    protected void append(E event) {
        if (!isInitialized && !lazyInitializeAzureClient()) {
            return;
        }

        try {
            if (!LocalDate.now().equals(currentLogDate)) {
                BlobServiceClient serviceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
                initializeBlobClient(serviceClient.getBlobContainerClient(containerName));
            }

            String message = event.toString() + "\n";
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            appendBlobClient.appendBlock(new ByteArrayInputStream(bytes), bytes.length);
        } catch (Exception e) {
            addError("Failed to append log line block to Azure Blob Container", e);
        }
    }

    public void setBlobPrefix(String blobPrefix) { this.blobPrefix = blobPrefix; }
}
