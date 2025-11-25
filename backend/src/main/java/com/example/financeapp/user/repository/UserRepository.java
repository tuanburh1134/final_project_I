package com.example.financeapp.user.repository;

import com.example.financeapp.user.entity.User;
import com.example.financeapp.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);   // để check "last admin"

    // 👇 THÊM DÒNG NÀY
    Optional<User> findByResetToken(String resetToken);
}

