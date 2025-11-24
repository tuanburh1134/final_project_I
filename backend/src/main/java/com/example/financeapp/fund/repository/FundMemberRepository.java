package com.example.financeapp.fund.repository;

import com.example.financeapp.fund.entity.FundMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundMemberRepository extends JpaRepository<FundMember, Long> {

    boolean existsByFund_FundIdAndMemberEmailIgnoreCase(Long fundId, String memberEmail);

    List<FundMember> findByFund_FundId(Long fundId);

    long countByFund_FundIdAndActiveIsTrue(Long fundId);

    Optional<FundMember> findByFund_FundIdAndMemberEmailIgnoreCase(Long fundId, String memberEmail);
}

