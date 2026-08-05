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

    // Remove @Value annotations. Logback uses the setter methods instead.
    private String connectionString;
    private String containerName;
    private String blobPrefix = "log-";

    private AppendBlobClient appendBlobClient;
    private LocalDate currentLogDate;

    @Override
    public void start() {
        // Logback leaves fields null if the XML property is missing
        if (connectionString == null || connectionString.trim().isEmpty() ||
                containerName == null || containerName.trim().isEmpty()) {
            addError("Connection string or Container name not set for AzureBlobAppender");
            return;
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

    // Critical: Logback uses these setters to inject properties from logback-spring.xml
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }
    public void setContainerName(String containerName) { this.containerName = containerName; }
    public void setBlobPrefix(String blobPrefix) { this.blobPrefix = blobPrefix; }
}
