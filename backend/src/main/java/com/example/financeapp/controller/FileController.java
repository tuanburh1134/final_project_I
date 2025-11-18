package com.example.financeapp.controller;

import com.example.financeapp.entity.User;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    /**
     * Upload ảnh hóa đơn
     * POST /files/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();

        try {
            Long userId = getCurrentUserId();
            String fileUrl = fileStorageService.storeImage(file, userId);

            res.put("success", true);
            res.put("message", "Upload ảnh thành công");
            res.put("fileUrl", fileUrl);
            return ResponseEntity.ok(res);

        } catch (IllegalArgumentException e) {
            res.put("success", false);
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("error", "Lỗi khi upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Xóa ảnh
     * DELETE /files/delete?fileUrl=...
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteImage(@RequestParam String fileUrl) {
        Map<String, Object> res = new HashMap<>();

        try {
            boolean deleted = fileStorageService.deleteImage(fileUrl);
            if (deleted) {
                res.put("success", true);
                res.put("message", "Xóa ảnh thành công");
            } else {
                res.put("success", false);
                res.put("error", "Không tìm thấy file hoặc không thể xóa");
            }
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("success", false);
            res.put("error", "Lỗi khi xóa file: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
}

