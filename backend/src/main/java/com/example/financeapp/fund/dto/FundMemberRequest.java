package com.example.financeapp.fund.dto;

import com.example.financeapp.fund.entity.FundMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class FundMemberRequest {

    private String fullName;

    @NotBlank(message = "Email thành viên không được để trống")
    @Email(message = "Email thành viên không hợp lệ")
    private String email;

    private FundMemberRole role = FundMemberRole.CONTRIBUTOR;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FundMemberRole getRole() {
        return role;
    }

    public void setRole(FundMemberRole role) {
        this.role = role;
    }
}

