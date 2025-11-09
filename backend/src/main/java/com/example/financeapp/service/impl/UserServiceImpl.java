package com.example.financeapp.service;

import com.example.financeapp.entity.User;
import com.example.financeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User updateUserProfile(Long id, String fullName, String avatar) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(fullName);
        user.setAvatar(avatar);
        return userRepository.save(user);
    }
}
