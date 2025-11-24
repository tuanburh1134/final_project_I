package com.example.financeapp.backup;

import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3CloudStorageService implements CloudStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public S3CloudStorageService(BackupProperties.S3Properties s3Properties) {
        if (!StringUtils.hasText(s3Properties.getBucket())) {
            throw new IllegalArgumentException("Bucket S3 không được để trống");
        }
        this.bucket = s3Properties.getBucket();

        AwsCredentialsProvider credentialsProvider;
        if (StringUtils.hasText(s3Properties.getAccessKey()) &&
                StringUtils.hasText(s3Properties.getSecretKey())) {
            credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey())
            );
        } else {
            credentialsProvider = DefaultCredentialsProvider.create();
        }

        Region region = StringUtils.hasText(s3Properties.getRegion())
                ? Region.of(s3Properties.getRegion())
                : Region.AP_SOUTHEAST_1;

        this.s3Client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Override
    public String store(String key, byte[] data, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        return "s3://" + bucket + "/" + key;
    }
}

