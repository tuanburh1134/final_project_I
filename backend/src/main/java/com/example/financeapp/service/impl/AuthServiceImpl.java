package com.example.financeapp.service.impl;

import com.example.financeapp.dto.RegisterRequest;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.VerificationToken;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.repository.VerificationTokenRepository;
import com.example.financeapp.service.AuthService;
import com.example.financeapp.security.JwtTokenUtil;
import com.example.financeapp.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được đăng ký.");
        }
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu và Xác nhận mật khẩu không khớp.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEnabled(false);

        userRepository.save(user);

        roleRepository.findByRoleName("USER").ifPresent(role -> {
        });

        String token = jwtTokenUtil.generateVerificationToken(user);

        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);

        sendActivationEmail(user.getEmail(), token);
    }

    @Override
    public void sendActivationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Kích hoạt Tài khoản FinanceApp");

        String activationUrl = baseUrl + "/api/auth/verify-account?token=" + token;

        String text = "Vui lòng nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn:\n"
                + activationUrl + "\n\n"
                + "Liên kết sẽ hết hạn sau 24 giờ.";

        message.setText(text);
        mailSender.send(message);
    }

    @Override
    @Transactional
    public boolean verifyAccount(String token) {
        // 1. Tìm token
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link xác thực không hợp lệ."));

        User user = verificationToken.getUser();

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Vui lòng yêu cầu lại email xác thực.");
        }

        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        return true;
    }
}