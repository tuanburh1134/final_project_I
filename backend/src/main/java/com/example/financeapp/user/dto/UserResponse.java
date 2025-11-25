package com.example.financeapp.user.dto;

import com.example.financeapp.security.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
    private Role role;
    private boolean locked;
    private boolean googleAccount;
    private boolean firstLogin;
}
