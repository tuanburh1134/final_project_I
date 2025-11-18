package com.example.financeapp.repository;

import com.example.financeapp.entity.Fund;
import com.example.financeapp.entity.FundType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundRepository extends JpaRepository<Fund, Long> {

    List<Fund> findByOwner_UserIdAndDeletedFalse(Long userId);

    List<Fund> findByFundMembers_User_UserIdAndDeletedFalse(Long userId);

    boolean existsByWallet_WalletIdAndDeletedFalse(Long walletId);

    Optional<Fund> findByFundIdAndDeletedFalse(Long fundId);

    List<Fund> findByFundTypeAndDeletedFalse(FundType fundType);
}

