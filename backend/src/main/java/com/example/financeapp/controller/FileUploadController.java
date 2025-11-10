package com.example.financeapp.controller;

import com.example.financeapp.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 📤 Upload ảnh hóa đơn
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Kiểm tra file có rỗng không
            if (file.isEmpty()) {
                response.put("error", "Vui lòng chọn file để upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra định dạng file (chỉ cho phép ảnh)
            if (!fileStorageService.isValidImageFile(file)) {
                response.put("error", "Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WEBP)");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra kích thước file (tối đa 5MB)
            if (!fileStorageService.isValidFileSize(file)) {
                response.put("error", "Kích thước file không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Lưu file và lấy tên file unique
            String filename = fileStorageService.storeFile(file);

            // Tạo URL để truy cập file
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(filename)
                    .toUriString();

            response.put("message", "Upload file thành công");
            response.put("filename", filename);
            response.put("fileUrl", fileDownloadUri);
            response.put("fileSize", file.getSize());
            response.put("fileType", file.getContentType());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Lỗi khi upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 📥 Download/View ảnh hóa đơn
     * GET /api/files/{filename}
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource(filename);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                System.out.println("Could not determine file type.");
            }

            // Fallback to default content type
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 🗑️ Xóa ảnh hóa đơn
     * DELETE /api/files/{filename}
     */
    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String filename) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = fileStorageService.deleteFile(filename);

            if (deleted) {
                response.put("message", "Xóa file thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Không thể xóa file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            response.put("error", "Lỗi khi xóa file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 📤 Upload nhiều ảnh cùng lúc (bonus feature)
     * POST /api/files/upload-multiple
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (files.length == 0) {
                response.put("error", "Vui lòng chọn ít nhất 1 file để upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Giới hạn tối đa 5 file mỗi lần upload
            if (files.length > 5) {
                response.put("error", "Chỉ được upload tối đa 5 file mỗi lần");
                return ResponseEntity.badRequest().body(response);
            }

            java.util.List<Map<String, String>> uploadedFiles = new java.util.ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty() && 
                    fileStorageService.isValidImageFile(file) && 
                    fileStorageService.isValidFileSize(file)) {
                    
                    String filename = fileStorageService.storeFile(file);
                    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/files/")
                            .path(filename)
                            .toUriString();

                    Map<String, String> fileInfo = new HashMap<>();
                    fileInfo.put("filename", filename);
                    fileInfo.put("fileUrl", fileDownloadUri);
                    uploadedFiles.add(fileInfo);
                }
            }

            response.put("message", "Upload " + uploadedFiles.size() + " file thành công");
            response.put("files", uploadedFiles);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Lỗi khi upload files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

