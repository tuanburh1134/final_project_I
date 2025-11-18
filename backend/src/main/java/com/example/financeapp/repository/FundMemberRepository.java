package com.example.financeapp.repository;

import com.example.financeapp.entity.FundMember;
import com.example.financeapp.entity.FundMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundMemberRepository extends JpaRepository<FundMember, Long> {

    List<FundMember> findByFund_FundId(Long fundId);

    List<FundMember> findByUser_UserId(Long userId);

    boolean existsByFund_FundIdAndUser_UserId(Long fundId, Long userId);

    Optional<FundMember> findByFund_FundIdAndUser_UserId(Long fundId, Long userId);

    int countByFund_FundIdAndStatus(Long fundId, FundMemberStatus status);
}

