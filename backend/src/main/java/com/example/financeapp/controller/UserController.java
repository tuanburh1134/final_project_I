package com.example.financeapp.controller;

import com.example.financeapp.entity.User;
import com.example.financeapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/{id}")
    public User updateProfile(@PathVariable Long id, @RequestBody User userData) {
        return userService.updateUserProfile(id, userData.getFullName(), userData.getAvatar());
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
