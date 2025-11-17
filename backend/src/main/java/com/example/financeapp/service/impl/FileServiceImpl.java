package com.example.financeapp.service.impl;

import com.example.financeapp.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public FileUploadResult uploadFile(MultipartFile file, String type) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }

        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File quá lớn. Kích thước tối đa: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Validate file type (chỉ chấp nhận ảnh)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh (jpg, png, gif, webp)");
        }

        // Validate type
        if (type == null || type.trim().isEmpty()) {
            type = "receipt"; // Default
        }
        type = type.toLowerCase().trim();

        // Tạo thư mục nếu chưa có
        Path uploadPath = Paths.get(uploadDir, type);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Lưu file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Tạo URL
        String url = "/files/" + type + "/" + uniqueFilename;

        return new FileUploadResult(
                url,
                uniqueFilename,
                originalFilename != null ? originalFilename : "unknown",
                file.getSize(),
                contentType
        );
    }

    @Override
    public boolean deleteFile(String type, String filename) throws IOException {
        if (type == null || filename == null || type.trim().isEmpty() || filename.trim().isEmpty()) {
            throw new RuntimeException("Type và filename không được để trống");
        }

        Path filePath = getFilePath(type, filename);
        if (!Files.exists(filePath)) {
            return false;
        }

        Files.delete(filePath);
        return true;
    }

    @Override
    public Path getFilePath(String type, String filename) {
        if (type == null || filename == null) {
            throw new RuntimeException("Type và filename không được để trống");
        }
        return Paths.get(uploadDir, type.toLowerCase().trim(), filename);
    }

    @Override
    public boolean fileExists(String type, String filename) {
        try {
            Path filePath = getFilePath(type, filename);
            return Files.exists(filePath) && Files.isRegularFile(filePath);
        } catch (Exception e) {
            return false;
        }
    }
}

