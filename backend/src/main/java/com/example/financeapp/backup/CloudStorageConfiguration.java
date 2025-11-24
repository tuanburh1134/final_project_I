package com.example.financeapp.backup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class CloudStorageConfiguration {

    @Bean
    public CloudStorageService cloudStorageService(BackupProperties properties) {
        if ("s3".equalsIgnoreCase(properties.getProvider())
                && properties.getS3() != null
                && StringUtils.hasText(properties.getS3().getBucket())) {
            return new S3CloudStorageService(properties.getS3());
        }
        return new LocalCloudStorageService();
    }
}

