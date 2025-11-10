package com.example.financeapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/transactions}")
    private String uploadDir;

    private Path fileStorageLocation;

    /**
     * Khởi tạo thư mục lưu trữ khi service được tạo
     */
    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            System.out.println(">>> [FileStorage] Thư mục lưu trữ: " + this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ file!", ex);
        }
    }

    /**
     * Lưu file vào server
     * @param file File từ request
     * @return Tên file đã lưu (unique filename)
     */
    public String storeFile(MultipartFile file) {
        // Lấy tên file gốc và làm sạch
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Kiểm tra file có hợp lệ không
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Tên file không hợp lệ: " + originalFilename);
            }

            // Tạo tên file unique để tránh trùng
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Copy file vào thư mục lưu trữ
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            System.out.println(">>> [FileStorage] Đã lưu file: " + uniqueFilename);
            return uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi lưu file: " + originalFilename, ex);
        }
    }

    /**
     * Load file từ server
     * @param filename Tên file cần load
     * @return Resource của file
     */
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File không tồn tại: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File không tồn tại: " + filename, ex);
        }
    }

    /**
     * Xóa file khỏi server
     * @param filename Tên file cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            System.out.println(">>> [FileStorage] Đã xóa file: " + filename);
            return true;
        } catch (IOException ex) {
            System.err.println(">>> [FileStorage] Lỗi khi xóa file: " + filename);
            return false;
        }
    }

    /**
     * Kiểm tra file có phải là ảnh hợp lệ không
     * @param file File cần kiểm tra
     * @return true nếu là ảnh
     */
    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Chỉ cho phép các định dạng ảnh phổ biến
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    /**
     * Kiểm tra kích thước file (tối đa 5MB)
     * @param file File cần kiểm tra
     * @return true nếu kích thước hợp lệ
     */
    public boolean isValidFileSize(MultipartFile file) {
        long maxSize = 5 * 1024 * 1024; // 5MB
        return file.getSize() <= maxSize;
    }
}

