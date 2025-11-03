package com.example.financeapp.service.impl;

import com.example.financeapp.dto.RegisterRequest;
import com.example.financeapp.entity.PasswordResetToken;
import com.example.financeapp.entity.User;
import com.example.financeapp.entity.VerificationToken;
import com.example.financeapp.repository.PasswordResetTokenRepository;
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
    private final PasswordResetTokenRepository resetRepo;

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
    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại."));
        String token = jwtTokenUtil.generateVerificationToken(user); // reuse, purpose=verification
        PasswordResetToken reset = new PasswordResetToken(token, user);
        resetRepo.save(reset);
        sendResetEmail(user.getEmail(), token);
    }

    private void sendResetEmail(String email, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email); msg.setSubject("Đặt lại mật khẩu FinanceApp");
        String url = baseUrl + "/reset-password?token=" + token;
        msg.setText("Nhấp link để đặt lại mật khẩu (hết hạn 15 phút):\n" + url);
        mailSender.send(msg);
    }

    @Override @Transactional
    public void resetPassword(String token, String newPwd, String confirmPwd) {
        if (!newPwd.equals(confirmPwd)) throw new RuntimeException("Mật khẩu không khớp.");
        PasswordResetToken reset = resetRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ."));
        if (reset.isExpired()) throw new RuntimeException("Token hết hạn.");
        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(newPwd));
        userRepository.save(user);
        resetRepo.delete(reset);
    }
}