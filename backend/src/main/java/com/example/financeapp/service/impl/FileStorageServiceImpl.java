package com.example.financeapp.service.impl;

import com.example.financeapp.service.FileStorageService;
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
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:52428800}") // 50MB default
    private long maxFileSize;

    // Các định dạng ảnh được phép
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public String storeImage(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (!isValidImage(file)) {
            throw new IllegalArgumentException("File phải là ảnh hợp lệ (JPEG, PNG, GIF, WEBP)");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Tạo thư mục nếu chưa có
        Path userDir = Paths.get(uploadDir, "transactions", userId.toString());
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Lưu file
        Path targetPath = userDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về URL relative để truy cập
        return "/uploads/transactions/" + userId + "/" + uniqueFilename;
    }

    @Override
    public boolean deleteImage(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }

            // Loại bỏ phần /uploads nếu có
            String relativePath = fileUrl;
            if (fileUrl.startsWith("/uploads/")) {
                relativePath = fileUrl.substring(1); // Bỏ dấu / đầu
            }

            Path filePath = Paths.get(uploadDir).resolve(relativePath.replace("uploads/", ""));
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Lỗi khi xóa file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }

        String contentType = file.getContentType().toLowerCase();
        return ALLOWED_IMAGE_TYPES.contains(contentType);
    }
}

