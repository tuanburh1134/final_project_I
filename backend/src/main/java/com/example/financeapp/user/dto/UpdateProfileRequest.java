package com.example.financeapp.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    // optional: nếu gửi thì phải hợp lệ
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 - 100 ký tự")
    private String fullName;

    // avatar có thể là URL, không validate sâu ở BE (FE/validator tự xử lý)
    private String avatar;
}

