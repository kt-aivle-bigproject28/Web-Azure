package com.bigproject.fic2toon.board;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final BlobServiceClient blobServiceClient;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    public UploadController(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @PostMapping
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
                    .getBlobClient(fileName);

            blobClient.upload(file.getInputStream(), file.getSize(), true);
            String fileUrl = blobClient.getBlobUrl();

            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }
}
