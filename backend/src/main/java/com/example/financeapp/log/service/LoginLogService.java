package com.example.financeapp.log.service;

import com.example.financeapp.log.entity.LoginLog;
import com.example.financeapp.log.repository.LoginLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    public LoginLogService(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    /**
     * Lưu 1 bản ghi login log mới
     */
    public void save(Long userId, String ipAddress, String userAgent) {
        LoginLog log = LoginLog.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        loginLogRepository.save(log);
    }

    /**
     * Lấy toàn bộ lịch sử đăng nhập của 1 user (mới -> cũ)
     */
    public List<LoginLog> getLogsByUser(Long userId) {
        return loginLogRepository.findByUserIdOrderByLoginTimeDesc(userId);
    }

    /**
     * Lấy N lần đăng nhập gần nhất của user (ví dụ dùng cho giao diện)
     */
    public List<LoginLog> getRecentLogsByUser(Long userId, int limit) {
        // Đơn giản dùng top10 trước, nếu muốn linh hoạt hơn có thể custom query
        return loginLogRepository.findTop10ByUserIdOrderByLoginTimeDesc(userId);
    }
}