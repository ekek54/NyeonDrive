package com.example.nyeondrive.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

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
    public S3Client storageClient() {
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(
                new AwsCredentials() {


                    @Override
                    public String accessKeyId() {
                        return accessKeyId;
                    }

                    @Override
                    public String secretAccessKey() {
                        return secretAccessKey;
                    }
                }
        );
        return S3Client.builder()
                .httpClientBuilder(ApacheHttpClient.builder()
                        .maxConnections(100)
                        .connectionTimeout(Duration.ofSeconds(5))
                )
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.of(region))
                .credentialsProvider(staticCredentialsProvider)
                .build();
    }
}
