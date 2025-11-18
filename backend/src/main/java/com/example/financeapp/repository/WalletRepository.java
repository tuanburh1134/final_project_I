package com.example.financeapp.repository;

import com.example.financeapp.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional; // <-- Cần thiết cho việc tìm kiếm chi tiết 1 đối tượng

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Query methods with soft delete filter (isDeleted = false)
    @Query("SELECT w FROM Wallet w WHERE w.user.userId = :userId AND w.isDeleted = false")
    List<Wallet> findByUser_UserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(w) > 0 FROM Wallet w WHERE w.walletName = :walletName AND w.user.userId = :userId AND w.isDeleted = false")
    boolean existsByWalletNameAndUser_UserId(@Param("walletName") String walletName, @Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.walletName = :walletName AND w.user.userId = :userId AND w.isDeleted = false")
    Wallet findByWalletNameAndUser_UserId(@Param("walletName") String walletName, @Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId AND w.user.userId = :userId AND w.isDeleted = false")
    Optional<Wallet> findByWalletIdAndUser_UserId(@Param("walletId") Long walletId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Wallet w SET w.isDefault = FALSE WHERE w.user.userId = :userId AND (:walletId IS NULL OR w.walletId != :walletId) AND w.isDeleted = false")
    void unsetDefaultWallet(@Param("userId") Long userId, @Param("walletId") Long walletId);

    @Modifying
    @Query("UPDATE Wallet w SET w.isDefault = TRUE WHERE w.walletId = :walletId AND w.user.userId = :userId AND w.isDeleted = false")
    void setDefaultWallet(@Param("userId") Long userId, @Param("walletId") Long walletId);

    // ✅ Lấy wallet với PESSIMISTIC LOCK để tránh race condition khi transfer/transaction (chỉ lấy chưa bị xóa)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId AND w.isDeleted = false")
    Optional<Wallet> findByIdWithLock(@Param("walletId") Long walletId);
    
    // Query để lấy wallet chưa bị xóa theo walletId (không filter theo userId)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId AND w.isDeleted = false")
    Optional<Wallet> findByIdNotDeleted(@Param("walletId") Long walletId);
    
    // Query để lấy cả ví đã bị xóa (cho soft delete operation)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :id")
    Optional<Wallet> findByIdIncludingDeleted(@Param("id") Long id);
}