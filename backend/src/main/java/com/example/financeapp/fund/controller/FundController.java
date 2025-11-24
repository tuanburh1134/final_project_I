package com.example.financeapp.fund.controller;

import com.example.financeapp.fund.dto.CreateFundRequest;
import com.example.financeapp.fund.dto.FundDashboardResponse;
import com.example.financeapp.fund.dto.FundDetailResponse;
import com.example.financeapp.fund.service.FundService;
import com.example.financeapp.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/funds")
public class FundController {

    @Autowired
    private FundService fundService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFund(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateFundRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = requireUserId(userDetails);
            FundDetailResponse result = fundService.createFund(userId, request);
            response.put("message", "Tạo quỹ thành công");
            response.put("fund", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.put("error", "Lỗi hệ thống: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getFundDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = requireUserId(userDetails);
            FundDashboardResponse dashboard = fundService.getFundDashboard(userId);
            response.put("data", dashboard);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.put("error", "Lỗi hệ thống: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{fundId}")
    public ResponseEntity<Map<String, Object>> getFundDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long fundId
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = requireUserId(userDetails);
            FundDetailResponse detail = fundService.getFundDetail(userId, fundId);
            response.put("fund", detail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.put("error", "Lỗi hệ thống: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Long requireUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null || userDetails.getUser().getUserId() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return userDetails.getUser().getUserId();
    }
}

