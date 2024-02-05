package com.example.nyeondrive.repository;

import com.example.nyeondrive.entity.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Repository
public class FileRepository {
    private final S3Client storageClient;

    public FileRepository(S3Client storageClient) {
        this.storageClient = storageClient;
    }

    public void uploadFile(File file) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket("nyeon-drive")
                .key(file.getName())
                .contentType(file.getType())
                .build();
        RequestBody requestBody = RequestBody
                .fromInputStream(file.getData(), file.getSize());
        PutObjectResponse putObjectResponse = storageClient.putObject(req, requestBody);
        System.out.println(putObjectResponse.toString());
    }
}
