package com.example.financeapp.backup.service;

public interface CloudStorageService {

    /**
     * Uploads binary data to cloud storage.
     *
     * @param key         remote key/path inside the bucket
     * @param data        payload bytes (optionally compressed by caller)
     * @param contentType mime type (e.g. application/gzip)
     * @return public (or logical) location string such as s3://bucket/key
     */
    String upload(String key, byte[] data, String contentType);
}

