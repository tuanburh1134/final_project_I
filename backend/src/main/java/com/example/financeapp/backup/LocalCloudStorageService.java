package com.example.financeapp.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalCloudStorageService implements CloudStorageService {

    private static final Path BASE_DIR = Paths.get("backup-storage");

    @Override
    public String store(String key, byte[] data, String contentType) {
        try {
            Path target = BASE_DIR.resolve(key).normalize();
            Files.createDirectories(target.getParent());
            Files.write(target, data);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Không thể ghi file backup", e);
        }
    }
}

