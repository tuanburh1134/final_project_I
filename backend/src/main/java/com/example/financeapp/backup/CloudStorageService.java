package com.example.financeapp.backup;

public interface CloudStorageService {

    /**
     * Store binary data to cloud storage.
     *
     * @param key  location key/path
     * @param data bytes to store
     * @param contentType MIME type
     * @return public/diagnostic location string
     */
    String store(String key, byte[] data, String contentType);
}

