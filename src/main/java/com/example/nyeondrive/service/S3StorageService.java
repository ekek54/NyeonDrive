package com.example.nyeondrive.service;

import com.example.nyeondrive.entity.File;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
public class S3StorageService implements StorageService {

    private final S3Client storageClient;

    public S3StorageService(S3Client storageClient) {
        this.storageClient = storageClient;
    }

    public void uploadFile(File file) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket("nyeon-drive")
                .key(String.valueOf(file.getId()))
                .contentType(file.getContentType())
                .build();
        RequestBody requestBody = RequestBody
                .fromInputStream(file.getInputStream(), file.getSize());
        PutObjectResponse putObjectResponse = storageClient.putObject(req, requestBody);
    }
}
