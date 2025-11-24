package com.example.financeapp.fund.entity;

import com.example.financeapp.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fund_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fund_id", "member_email"}))
public class FundMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fund_member_id")
    private Long fundMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "member_name", length = 150)
    private String memberName;

    @Column(name = "member_email", length = 150, nullable = false)
    private String memberEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private FundMemberRole role = FundMemberRole.VIEWER;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public FundMemberRole getRole() {
        return role;
    }

    public void setRole(FundMemberRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}

