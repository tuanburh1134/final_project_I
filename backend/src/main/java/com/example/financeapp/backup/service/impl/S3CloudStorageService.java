package com.example.financeapp.backup.service.impl;

import com.example.financeapp.backup.config.CloudStorageProperties;
import com.example.financeapp.backup.service.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3CloudStorageService implements CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3CloudStorageService.class);

    private final S3Client s3Client;
    private final CloudStorageProperties properties;

    public S3CloudStorageService(S3Client s3Client, CloudStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public String upload(String key, byte[] data, String contentType) {
        if (!StringUtils.hasText(properties.getBucket())) {
            throw new IllegalStateException("CLOUD_BACKUP_BUCKET chưa được cấu hình. Vui lòng thiết lập biến môi trường.");
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        String location = "s3://" + properties.getBucket() + "/" + key;
        log.info("Uploaded backup to {}", location);
        return location;
    }
}

