package com.example.financeapp.fund.repository;

import com.example.financeapp.fund.entity.Fund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FundRepository extends JpaRepository<Fund, Long> {

    boolean existsByWallet_WalletId(Long walletId);

    @Query("""
            SELECT DISTINCT f
            FROM Fund f
            LEFT JOIN f.members m
            WHERE f.owner.userId = :userId
               OR (m.user.userId = :userId AND m.active = true)
            """)
    List<Fund> findAccessibleFunds(@Param("userId") Long userId);

    @Query("""
            SELECT f
            FROM Fund f
            LEFT JOIN FETCH f.members m
            WHERE f.fundId = :fundId
            """)
    Fund findFundWithMembers(@Param("fundId") Long fundId);
}

