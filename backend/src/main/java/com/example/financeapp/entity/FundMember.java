package com.example.financeapp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fund_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fund_id", "user_id"}))
public class FundMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fund_member_id")
    private Long fundMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private FundMemberRole role = FundMemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FundMemberStatus status = FundMemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public FundMember() {
    }

    public FundMember(Fund fund, User user, FundMemberRole role, FundMemberStatus status) {
        this.fund = fund;
        this.user = user;
        this.role = role;
        this.status = status;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getFundMemberId() {
        return fundMemberId;
    }

    public void setFundMemberId(Long fundMemberId) {
        this.fundMemberId = fundMemberId;
    }

    public Fund getFund() {
        return fund;
    }

    public void setFund(Fund fund) {
        this.fund = fund;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FundMemberRole getRole() {
        return role;
    }

    public void setRole(FundMemberRole role) {
        this.role = role;
    }

    public FundMemberStatus getStatus() {
        return status;
    }

    public void setStatus(FundMemberStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}

