package com.example.financeapp.backup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CloudStorageProperties {

    @Value("${CLOUD_BACKUP_BUCKET:}")
    private String bucket;

    @Value("${CLOUD_BACKUP_PREFIX:backups}")
    private String prefix;

    @Value("${AWS_REGION:ap-southeast-1}")
    private String region;

    public String getBucket() {
        return bucket;
    }

    public String getPrefix() {
        return StringUtils.hasText(prefix) ? prefix : "backups";
    }

    public String getRegion() {
        return region;
    }
}

