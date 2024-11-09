package com.example.nyeondrive.file.infrastructure;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.nyeondrive.exception.error.BadRequestException;
import com.example.nyeondrive.file.entity.File;
import java.io.InputStream;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class S3FileStorage implements FileStorage {

    private final AmazonS3 s3;

    @Value("${cloud.S3.region}")
    private String region;

    public S3FileStorage(AmazonS3 s3) {
        this.s3 = s3;
    }

    public void streamUpload(File file, InputStream fileStream, UUID userId) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        PutObjectRequest req = new PutObjectRequest(
                userId.toString(),
                file.getId().toString(),
                fileStream,
                objectMetadata
        );
        try {
            s3.putObject(req);
        } catch (AmazonS3Exception e) {
            throw new RuntimeException("Failed to upload file on S3");
        } catch (SdkClientException e) {
            throw new RuntimeException("Failed to connect to S3");
        }
    }

    public void createStorage(UUID userId) {
        if (s3.doesBucketExistV2(userId.toString())) {
            throw new BadRequestException("Bucket already exists");
        }
        s3.createBucket(userId.toString());
    }
}
