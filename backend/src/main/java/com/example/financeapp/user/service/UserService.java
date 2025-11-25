package com.example.financeapp.user.service;

import com.example.financeapp.user.dto.UpdateProfileRequest;
import com.example.financeapp.user.dto.UserResponse;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import com.example.financeapp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Lấy profile hiện tại
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toResponse(user);
    }

    // Cập nhật profile (tên + avatar)
    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request,
                                        CustomUserDetails currentUser) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Nếu fullName gửi lên != null thì mới cập nhật
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        // Nếu avatar gửi lên != null thì mới cập nhật
        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            user.setAvatar(request.getAvatar());
        }

        User saved = userRepository.save(user);   // ⭐ đảm bảo ghi DB

        return toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());          // ⭐ ĐỪNG QUÊN DÒNG NÀY
        dto.setRole(user.getRole());
        dto.setLocked(user.isLocked());
        dto.setGoogleAccount(user.isGoogleAccount());
        dto.setFirstLogin(user.isFirstLogin());
        return dto;
    }
}
