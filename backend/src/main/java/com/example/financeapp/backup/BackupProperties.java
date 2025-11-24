package com.example.financeapp.backup;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "backup")
@Getter
@Setter
public class BackupProperties {

    /**
     * Provider: local | s3
     */
    private String provider = "local";

    private S3Properties s3 = new S3Properties();

    @Getter
    @Setter
    public static class S3Properties {
        /**
         * AWS S3 bucket name
         */
        private String bucket;
        /**
         * AWS region, e.g. ap-southeast-1
         */
        private String region;
        /**
         * Access key (optional – can use default credentials provider instead)
         */
        private String accessKey;
        /**
         * Secret key (optional – can use default credentials provider instead)
         */
        private String secretKey;
    }
}

