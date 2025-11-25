package com.example.financeapp.log.repository;

import com.example.financeapp.log.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    /**
     * Lấy lịch sử đăng nhập của 1 user (mới nhất trước)
     */
    List<LoginLog> findByUserIdOrderByLoginTimeDesc(Long userId);

    /**
     * Lấy N bản ghi mới nhất
     */
    List<LoginLog> findTop10ByUserIdOrderByLoginTimeDesc(Long userId);
}
