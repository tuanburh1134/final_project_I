package com.example.financeapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity cho chuyển tiền nội bộ giữa các ví
 * Không cần category_id như transactions thông thường
 */
@Entity
@Table(
    name = "wallet_transfers",
    indexes = {
        @Index(name = "idx_from_wallet", columnList = "from_wallet_id"),
        @Index(name = "idx_to_wallet", columnList = "to_wallet_id"),
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_transfer_date", columnList = "transfer_date"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
public class WalletTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    // ===== WALLET INFO =====
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id", nullable = false)
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id", nullable = false)
    private Wallet toWallet;

    // ===== AMOUNT INFO =====
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    // ===== BALANCE TRACKING =====
    
    @Column(name = "from_balance_before", precision = 15, scale = 2)
    private BigDecimal fromBalanceBefore;

    @Column(name = "from_balance_after", precision = 15, scale = 2)
    private BigDecimal fromBalanceAfter;

    @Column(name = "to_balance_before", precision = 15, scale = 2)
    private BigDecimal toBalanceBefore;

    @Column(name = "to_balance_after", precision = 15, scale = 2)
    private BigDecimal toBalanceAfter;

    // ===== METADATA =====
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người thực hiện chuyển tiền

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // URL ảnh hóa đơn tùy chọn

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status = TransferStatus.COMPLETED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ===== ENUM =====
    
    public enum TransferStatus {
        COMPLETED,   // Hoàn thành
        PENDING,     // Đang chờ (cho tương lai nếu cần approval)
        CANCELLED    // Đã hủy
    }

    // ===== LIFECYCLE =====
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== CONSTRUCTORS =====
    
    public WalletTransfer() {
    }

    public WalletTransfer(Wallet fromWallet, Wallet toWallet, BigDecimal amount, 
                          String currencyCode, User user, String note) {
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.user = user;
        this.note = note;
        this.transferDate = LocalDateTime.now();
        this.status = TransferStatus.COMPLETED;
    }

    // ===== GETTERS & SETTERS =====
    
    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Wallet getFromWallet() {
        return fromWallet;
    }

    public void setFromWallet(Wallet fromWallet) {
        this.fromWallet = fromWallet;
    }

    public Wallet getToWallet() {
        return toWallet;
    }

    public void setToWallet(Wallet toWallet) {
        this.toWallet = toWallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getFromBalanceBefore() {
        return fromBalanceBefore;
    }

    public void setFromBalanceBefore(BigDecimal fromBalanceBefore) {
        this.fromBalanceBefore = fromBalanceBefore;
    }

    public BigDecimal getFromBalanceAfter() {
        return fromBalanceAfter;
    }

    public void setFromBalanceAfter(BigDecimal fromBalanceAfter) {
        this.fromBalanceAfter = fromBalanceAfter;
    }

    public BigDecimal getToBalanceBefore() {
        return toBalanceBefore;
    }

    public void setToBalanceBefore(BigDecimal toBalanceBefore) {
        this.toBalanceBefore = toBalanceBefore;
    }

    public BigDecimal getToBalanceAfter() {
        return toBalanceAfter;
    }

    public void setToBalanceAfter(BigDecimal toBalanceAfter) {
        this.toBalanceAfter = toBalanceAfter;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

