package com.example.financeapp.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeRoleRequest {

    @NotBlank(message = "Role không được để trống")
    private String role; // "USER" hoặc "ADMIN"
}


