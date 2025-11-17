package com.example.financeapp.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {
    /**
     * Upload file và trả về thông tin file đã lưu
     * @param file File cần upload
     * @param type Loại file (receipt, avatar, etc.)
     * @return Thông tin file đã upload (filename, url, size, contentType)
     */
    FileUploadResult uploadFile(MultipartFile file, String type) throws IOException;

    /**
     * Xóa file
     * @param type Loại file
     * @param filename Tên file
     * @return true nếu xóa thành công
     */
    boolean deleteFile(String type, String filename) throws IOException;

    /**
     * Lấy đường dẫn file
     * @param type Loại file
     * @param filename Tên file
     * @return Path của file
     */
    Path getFilePath(String type, String filename);

    /**
     * Kiểm tra file có tồn tại không
     * @param type Loại file
     * @param filename Tên file
     * @return true nếu file tồn tại
     */
    boolean fileExists(String type, String filename);

    /**
     * Class để trả về kết quả upload
     */
    class FileUploadResult {
        private String url;
        private String filename;
        private String originalFilename;
        private long size;
        private String contentType;

        public FileUploadResult(String url, String filename, String originalFilename, long size, String contentType) {
            this.url = url;
            this.filename = filename;
            this.originalFilename = originalFilename;
            this.size = size;
            this.contentType = contentType;
        }

        // Getters
        public String getUrl() { return url; }
        public String getFilename() { return filename; }
        public String getOriginalFilename() { return originalFilename; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
    }
}

