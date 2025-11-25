package com.example.financeapp.user.controller;

import com.example.financeapp.user.dto.UpdateProfileRequest;
import com.example.financeapp.user.dto.UserResponse;
import com.example.financeapp.user.service.UserService;
import com.example.financeapp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    // GET /api/users/me
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UserResponse res = userService.getMyProfile(currentUser);
        return ResponseEntity.ok(res);
    }

    // PUT /api/users/me
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UserResponse res = userService.updateMyProfile(request, currentUser);
        return ResponseEntity.ok(res);
    }
}

