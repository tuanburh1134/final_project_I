package com.example.financeapp.wallet.repository;

import com.example.financeapp.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // dùng user.id
    List<Wallet> findByUser_Id(Long userId);

    boolean existsByWalletNameAndUser_Id(String walletName, Long userId);

    Wallet findByWalletNameAndUser_Id(String walletName, Long userId);

    Optional<Wallet> findByWalletIdAndUser_Id(Long walletId, Long userId);

    @Modifying
    @Query("""
        UPDATE Wallet w
        SET w.isDefault = FALSE
        WHERE w.user.id = :userId
          AND (:walletId IS NULL OR w.walletId <> :walletId)
    """)
    void unsetDefaultWallet(@Param("userId") Long userId,
                            @Param("walletId") Long walletId);

    @Modifying
    @Query("""
        UPDATE Wallet w
        SET w.isDefault = TRUE
        WHERE w.walletId = :walletId
          AND w.user.id = :userId
    """)
    void setDefaultWallet(@Param("userId") Long userId,
                          @Param("walletId") Long walletId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId")
    Optional<Wallet> findByIdWithLock(@Param("walletId") Long walletId);
}
