package com.example.financeapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity cho giao dịch đặt lịch hẹn
 * Tự động tạo transaction vào thời điểm đã hẹn
 */
@Entity
@Table(
    name = "scheduled_transactions",
    indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_wallet", columnList = "wallet_id"),
        @Index(name = "idx_scheduled_date", columnList = "scheduled_date"),
        @Index(name = "idx_status", columnList = "status")
    }
)
public class ScheduledTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduled_id")
    private Long scheduledId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate; // Thời điểm sẽ tự động tạo transaction

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ScheduledStatus status = ScheduledStatus.PENDING;

    @Column(name = "executed_at")
    private LocalDateTime executedAt; // Thời điểm đã thực hiện (nếu đã execute)

    @Column(name = "created_transaction_id")
    private Long createdTransactionId; // ID của transaction đã được tạo tự động

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ===== ENUM =====
    
    public enum ScheduledStatus {
        PENDING,    // Đang chờ thực hiện
        EXECUTED,   // Đã thực hiện
        CANCELLED   // Đã hủy
    }

    // ===== LIFECYCLE =====
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== CONSTRUCTORS =====
    
    public ScheduledTransaction() {
    }

    // ===== GETTERS & SETTERS =====
    
    public Long getScheduledId() {
        return scheduledId;
    }

    public void setScheduledId(Long scheduledId) {
        this.scheduledId = scheduledId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
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

    public ScheduledStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduledStatus status) {
        this.status = status;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public Long getCreatedTransactionId() {
        return createdTransactionId;
    }

    public void setCreatedTransactionId(Long createdTransactionId) {
        this.createdTransactionId = createdTransactionId;
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

