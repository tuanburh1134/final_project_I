package com.example.financeapp.service.impl;

import com.example.financeapp.dto.UpdateUserRequest;
import com.example.financeapp.entity.User;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.service.FileStorageService;
import com.example.financeapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public User updateUserInfo(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        // Cập nhật tên
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        return userRepository.save(user);
    }

    @Override
    public User updateAvatar(String email, MultipartFile avatarFile) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        try {
            // Xóa avatar cũ (nếu có)
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                fileStorageService.deleteAvatar(user.getAvatarUrl());
            }

            // Lưu avatar mới
            String avatarUrl = fileStorageService.saveAvatar(avatarFile, user.getUserId());
            user.setAvatarUrl(avatarUrl);

            return userRepository.save(user);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload avatar: " + e.getMessage());
        }
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    }
}

