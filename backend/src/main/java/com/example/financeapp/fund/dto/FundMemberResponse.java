package com.example.financeapp.fund.dto;

import com.example.financeapp.fund.entity.FundMember;
import com.example.financeapp.fund.entity.FundMemberRole;

public class FundMemberResponse {

    private Long userId;
    private String fullName;
    private String email;
    private FundMemberRole role;

    public static FundMemberResponse from(FundMember member) {
        FundMemberResponse response = new FundMemberResponse();
        response.setUserId(member.getUser() != null ? member.getUser().getUserId() : null);
        response.setFullName(member.getMemberName());
        response.setEmail(member.getMemberEmail());
        response.setRole(member.getRole());
        return response;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

