package com.example.financeapp.controller;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.Fund;
import com.example.financeapp.entity.FundMember;
import com.example.financeapp.security.CustomUserDetails;
import com.example.financeapp.service.FundService;
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

    @GetMapping("/overview")
    public ResponseEntity<FundOverviewResponse> getOverview(@AuthenticationPrincipal CustomUserDetails userDetails) {
        FundOverviewResponse response = fundService.getMyFundsOverview(userDetails.getUser().getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fundId}")
    public ResponseEntity<FundDetailResponse> getFundDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @PathVariable Long fundId) {
        FundDetailResponse detail = fundService.getFundDetail(userDetails.getUser().getUserId(), fundId);
        return ResponseEntity.ok(detail);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFund(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @Valid @RequestBody CreateFundRequest request) {
        Fund fund = fundService.createFund(userDetails.getUser().getUserId(), request);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Tạo quỹ thành công");
        res.put("fundId", fund.getFundId());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{fundId}")
    public ResponseEntity<Map<String, Object>> updateFund(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @PathVariable Long fundId,
                                                          @Valid @RequestBody UpdateFundRequest request) {
        Fund fund = fundService.updateFund(userDetails.getUser().getUserId(), fundId, request);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Cập nhật quỹ thành công");
        res.put("fundId", fund.getFundId());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{fundId}/close")
    public ResponseEntity<Map<String, Object>> closeFund(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @PathVariable Long fundId) {
        fundService.closeFund(userDetails.getUser().getUserId(), fundId);
        return ResponseEntity.ok(Map.of("message", "Đóng quỹ thành công"));
    }

    @DeleteMapping("/{fundId}")
    public ResponseEntity<Map<String, Object>> deleteFund(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @PathVariable Long fundId) {
        fundService.deleteFund(userDetails.getUser().getUserId(), fundId);
        return ResponseEntity.ok(Map.of("message", "Xóa quỹ thành công"));
    }

    @PostMapping("/{fundId}/members")
    public ResponseEntity<Map<String, Object>> addMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @PathVariable Long fundId,
                                                         @Valid @RequestBody FundMemberInviteRequest request) {
        FundMember member = fundService.addMember(userDetails.getUser().getUserId(), fundId, request);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Thêm thành viên thành công");
        res.put("memberId", member.getFundMemberId());
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{fundId}/members/{memberId}")
    public ResponseEntity<Map<String, Object>> removeMember(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @PathVariable Long fundId,
                                                            @PathVariable Long memberId) {
        fundService.removeMember(userDetails.getUser().getUserId(), fundId, memberId);
        return ResponseEntity.ok(Map.of("message", "Xóa thành viên khỏi quỹ thành công"));
    }
}

