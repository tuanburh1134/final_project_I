package com.example.financeapp.wallet.entity;

import com.example.financeapp.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallet_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "user_id"})
)
public class WalletMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY) // Nên thêm LAZY để tối ưu hiệu năng
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private WalletRole role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    // Field mới
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.PENDING; // Mặc định là Pending

    // Enum trạng thái
    public enum MemberStatus {
        PENDING,
        ACCEPTED
    }

    // Enum cho roles
    public enum WalletRole {
        OWNER,
        ADMIN,
        EDITOR,
        VIEWER
    }

    // Constructors
    public WalletMember() {
    }

    // --- SỬA LỖI 1: Constructor đầy đủ 4 tham số (Dùng cho tính năng mời thành viên) ---
    public WalletMember(Wallet wallet, User user, WalletRole role, MemberStatus status) {
        System.out.println(">>> DEBUG: Goi Constructor 4 tham so. Status truyen vao la: " + status);
        this.wallet = wallet;
        this.user = user;
        this.role = role;
        this.status = status;
        this.joinedAt = LocalDateTime.now();
    }

    // --- SỬA LỖI 2: Constructor 3 tham số (Dùng cho Owner khi tạo ví) ---
    public WalletMember(Wallet wallet, User user, WalletRole role) {
        System.out.println(">>> DEBUG: Goi Constructor 3 tham so (Default ACCEPTED)");
        this.wallet = wallet;
        this.user = user;
        this.role = role;
        this.status = MemberStatus.ACCEPTED; // Mặc định Accepted nếu dùng constructor này
        this.joinedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WalletRole getRole() {
        return role;
    }

    public void setRole(WalletRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    // Helper methods
    public boolean isOwner() {
        return this.role == WalletRole.OWNER;
    }

    public boolean isAdmin() {
        return this.role == WalletRole.ADMIN;
    }

    public boolean isEditor() {return this.role == WalletRole.EDITOR;}

    public boolean isViewer() {return this.role == WalletRole.VIEWER;}
}