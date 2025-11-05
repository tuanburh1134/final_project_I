package com.example.financeapp.controller;

import com.example.financeapp.dto.UpdateUserRequest;
import com.example.financeapp.entity.User;
import com.example.financeapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Lấy thông tin người dùng hiện tại
     * GET /user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName(); // Lấy email từ JWT token
            User user = userService.getUserByEmail(email);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatarUrl", user.getAvatarUrl());
            userInfo.put("provider", user.getProvider());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("updatedAt", user.getUpdatedAt());

            response.put("success", true);
            response.put("user", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Cập nhật thông tin cá nhân (tên)
     * PUT /user/update
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            User updatedUser = userService.updateUserInfo(email, request);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", updatedUser.getUserId());
            userInfo.put("fullName", updatedUser.getFullName());
            userInfo.put("email", updatedUser.getEmail());
            userInfo.put("avatarUrl", updatedUser.getAvatarUrl());
            userInfo.put("updatedAt", updatedUser.getUpdatedAt());

            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            response.put("user", userInfo);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật avatar
     * POST /user/avatar
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> updateAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();

            if (avatarFile.isEmpty()) {
                response.put("success", false);
                response.put("error", "Vui lòng chọn file avatar");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User updatedUser = userService.updateAvatar(email, avatarFile);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", updatedUser.getUserId());
            userInfo.put("fullName", updatedUser.getFullName());
            userInfo.put("email", updatedUser.getEmail());
            userInfo.put("avatarUrl", updatedUser.getAvatarUrl());
            userInfo.put("updatedAt", updatedUser.getUpdatedAt());

            response.put("success", true);
            response.put("message", "Cập nhật avatar thành công");
            response.put("user", userInfo);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật cả tên và avatar cùng lúc
     * POST /user/update-profile
     */
    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);

            // Cập nhật tên nếu có
            if (fullName != null && !fullName.trim().isEmpty()) {
                UpdateUserRequest request = new UpdateUserRequest();
                request.setFullName(fullName.trim());
                user = userService.updateUserInfo(email, request);
            }

            // Cập nhật avatar nếu có
            if (avatarFile != null && !avatarFile.isEmpty()) {
                user = userService.updateAvatar(email, avatarFile);
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatarUrl", user.getAvatarUrl());
            userInfo.put("updatedAt", user.getUpdatedAt());

            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            response.put("user", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

