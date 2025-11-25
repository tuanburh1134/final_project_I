package com.example.financeapp.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO để trả về thông tin wallet có kèm role của user
 */@Data
public class SharedWalletDTO {
    private Long walletId;
    private String walletName;
    private String walletType; // ✨ NEW: "PERSONAL" hoặc "GROUP"
    private String currencyCode;
    private BigDecimal balance;
    private String description;
    private String myRole; // Role của user hiện tại trong wallet này
    private int totalMembers;
    private int transactionCount; // Số lượng giao dịch trong ví
    @JsonProperty("isDefault")
    private boolean isDefault; // ✨ NEW: Ví có phải là ví mặc định không
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Constructors
    public SharedWalletDTO() {
    }


}

