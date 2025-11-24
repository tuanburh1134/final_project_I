package com.example.financeapp.backup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class CloudStorageConfig {

    private final CloudStorageProperties properties;

    public CloudStorageConfig(CloudStorageProperties properties) {
        this.properties = properties;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

