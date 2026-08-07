package com.employee.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.AppendBlobClient;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class AzureBlobAppender<E extends DeferredProcessingAware> extends AppenderBase<E> {

    // Remove @Value annotations. Logback uses the setter methods instead.
    @Value("${azure.storage.connection-string:}")
    private String connectionString;
    @Value("${azure.storage.container-name:}")
    private String containerName;
    private String blobPrefix = "log-";

    private AppendBlobClient appendBlobClient;
    private LocalDate currentLogDate;

    @Override
    public void start() {
        // Check if the connection string is empty or contains the unparsed placeholder string
        if (connectionString == null || connectionString.trim().isEmpty() ||
                connectionString.contains("AZURE_STORAGE_CONNECTION_STRING") ||
                containerName == null || containerName.trim().isEmpty()) {

            // Console fallback warning instead of a hard crash
            addWarn("Azure Blob credentials not configured or invalid. Azure Logging is disabled.");
            return; // Exits gracefully, letting the rest of Spring Boot boot normally
        }
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }

            initializeBlobClient(containerClient);
            super.start();
        } catch (Exception e) {
            addError("Failed to initialize Azure Blob Client", e);
        }
    }

    private synchronized void initializeBlobClient(BlobContainerClient containerClient) {
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
        try {
            if (!LocalDate.now().equals(currentLogDate)) {
                BlobServiceClient serviceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
                initializeBlobClient(serviceClient.getBlobContainerClient(containerName));
            }

            String message = event.toString() + "\n";
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

            appendBlobClient.appendBlock(new ByteArrayInputStream(bytes), bytes.length);
        } catch (Exception e) {
            addError("Failed to append log to Azure Blob", e);
        }
    }

}
