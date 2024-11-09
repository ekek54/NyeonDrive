package com.example.nyeondrive.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class StorageConfig {
    @Value("${cloud.S3.accessKeyId}")
    private String accessKeyId;

    @Value("${cloud.S3.secretAccessKey}")
    private String secretAccessKey;

    @Value("${cloud.S3.endpointUrl}")
    private String endpointUrl;

    @Value("${cloud.S3.region}")
    private String region;

    @Bean
    public AmazonS3 storageClient() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(endpointUrl, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey)))
                .build();
    }
}
