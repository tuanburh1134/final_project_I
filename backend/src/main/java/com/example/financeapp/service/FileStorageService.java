package com.example.financeapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    private final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Lưu file avatar và trả về đường dẫn tương đối
     */
    public String saveAvatar(MultipartFile file, Long userId) throws IOException {
        // Kiểm tra file có rỗng không
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File không được vượt quá 5MB");
        }

        // Lấy extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh: jpg, jpeg, png, gif, webp");
        }

        // Tạo tên file mới (tránh trùng lặp)
        String newFileName = "avatar_" + userId + "_" + UUID.randomUUID() + "." + extension;

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Lưu file
        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn tương đối (để lưu vào DB)
        return "/" + uploadDir + "/" + newFileName;
    }

    /**
     * Xóa avatar cũ (nếu có)
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        try {
            // Bỏ dấu "/" ở đầu nếu có
            String relativePath = avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl;
            Path filePath = Paths.get(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Không thể xóa file avatar cũ: " + e.getMessage());
        }
    }

    /**
     * Lấy extension của file
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Kiểm tra extension có hợp lệ không
     */
    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}

