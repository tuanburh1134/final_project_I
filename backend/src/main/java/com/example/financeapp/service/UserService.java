package com.example.financeapp.service;

import com.example.financeapp.dto.UpdateUserRequest;
import com.example.financeapp.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    
    /**
     * Cập nhật thông tin người dùng
     */
    User updateUserInfo(String email, UpdateUserRequest request);
    
    /**
     * Cập nhật avatar
     */
    User updateAvatar(String email, MultipartFile avatarFile);
    
    /**
     * Lấy thông tin user theo email
     */
    User getUserByEmail(String email);
}

