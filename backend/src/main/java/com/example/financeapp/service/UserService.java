package com.example.financeapp.service;

import com.example.financeapp.entity.User;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(Long id);
    User updateUserProfile(Long id, String fullName, String avatar);
}
