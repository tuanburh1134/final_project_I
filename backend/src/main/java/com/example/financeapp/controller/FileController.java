package com.example.financeapp.controller;

import com.example.financeapp.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;

    // ========================== UPLOAD FILE ==========================

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "receipt") String type) {
        
        Map<String, Object> res = new HashMap<>();
        
        try {
            // Kiểm tra authentication (chỉ user đã đăng nhập mới upload được)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email == null || "anonymousUser".equals(email)) {
                res.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(res);
            }

            FileService.FileUploadResult result = fileService.uploadFile(file, type);
            
            res.put("message", "Upload file thành công");
            res.put("url", result.getUrl());
            res.put("filename", result.getFilename());
            res.put("originalFilename", result.getOriginalFilename());
            res.put("size", result.getSize());
            res.put("contentType", result.getContentType());
            
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (IOException e) {
            res.put("error", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ========================== DELETE FILE ==========================

    @DeleteMapping("/{type}/{filename}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable String type,
            @PathVariable String filename) {
        
        Map<String, Object> res = new HashMap<>();
        
        try {
            // Kiểm tra authentication
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email == null || "anonymousUser".equals(email)) {
                res.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(res);
            }

            boolean deleted = fileService.deleteFile(type, filename);
            
            if (deleted) {
                res.put("message", "Xóa file thành công");
                return ResponseEntity.ok(res);
            } else {
                res.put("error", "File không tồn tại");
                return ResponseEntity.status(404).body(res);
            }
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (IOException e) {
            res.put("error", "Lỗi khi xóa file: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ========================== GET FILE (PUBLIC) ==========================

    @GetMapping("/{type}/{filename}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String type,
            @PathVariable String filename) {
        
        try {
            Path filePath = fileService.getFilePath(type, filename);
            
            if (!fileService.fileExists(type, filename)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Xác định content type
            String contentType = "application/octet-stream";
            try {
                String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                switch (fileExtension) {
                    case "jpg":
                    case "jpeg":
                        contentType = "image/jpeg";
                        break;
                    case "png":
                        contentType = "image/png";
                        break;
                    case "gif":
                        contentType = "image/gif";
                        break;
                    case "webp":
                        contentType = "image/webp";
                        break;
                }
            } catch (Exception e) {
                // Giữ nguyên default content type
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

