package com.example.financeapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service để xử lý upload và lưu trữ file
 */
public interface FileStorageService {
    
    /**
     * Lưu file ảnh và trả về URL để truy cập
     * @param file File cần upload
     * @param userId ID của user (để tổ chức thư mục)
     * @return URL của file đã lưu
     * @throws IOException Nếu có lỗi khi lưu file
     */
    String storeImage(MultipartFile file, Long userId) throws IOException;
    
    /**
     * Xóa file ảnh
     * @param fileUrl URL của file cần xóa
     * @return true nếu xóa thành công
     */
    boolean deleteImage(String fileUrl);
    
    /**
     * Kiểm tra file có phải là ảnh hợp lệ không
     * @param file File cần kiểm tra
     * @return true nếu là ảnh hợp lệ
     */
    boolean isValidImage(MultipartFile file);
}

