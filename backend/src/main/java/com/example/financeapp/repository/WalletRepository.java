package com.example.financeapp.repository;

import com.example.financeapp.entity.User;
import com.example.financeapp.entity.Wallet;
import com.example.financeapp.entity.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    // Tìm tất cả ví của user
    List<Wallet> findByUser(User user);
    
    // Tìm tất cả ví active của user
    List<Wallet> findByUserAndIsActive(User user, boolean isActive);
    
    // Tìm ví theo user và loại ví
    List<Wallet> findByUserAndWalletType(User user, WalletType walletType);
    
    // Tìm ví theo ID và user (để đảm bảo user chỉ truy cập ví của mình)
    Optional<Wallet> findByWalletIdAndUser(Long walletId, User user);
    
    // Đếm số ví của user
    long countByUser(User user);
}

