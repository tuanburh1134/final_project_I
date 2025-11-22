package com.example.financeapp.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // Đăng ký
    public void sendRegistrationVerificationEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Xác minh tài khoản đăng ký");
        msg.setText("Mã xác minh: " + code + "\nHiệu lực 60 giây.");
        mailSender.send(msg);
    }

    // Khôi phục mật khẩu
    public void sendPasswordResetEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Khôi phục mật khẩu");
        msg.setText("Mã xác thực: " + code + "\nHiệu lực 60 giây.\nBỏ qua nếu không yêu cầu.");
        mailSender.send(msg);
    }

    public void sendBudgetWarningEmail(String to, String budgetName, BigDecimal remaining, BigDecimal amountLimit) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("⚠️ Sắp vượt ngân sách \"" + budgetName + "\"");
        msg.setText(String.format(
                "Ngân sách \"%s\" chỉ còn %.2f / %.2f.\nVui lòng kiểm soát chi tiêu để không vượt hạn mức.",
                budgetName,
                remaining != null ? remaining.doubleValue() : 0.0,
                amountLimit != null ? amountLimit.doubleValue() : 0.0
        ));
        mailSender.send(msg);
    }

    public void sendBudgetExceededEmail(String to, String budgetName, BigDecimal spent, BigDecimal amountLimit) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("❌ Đã vượt ngân sách \"" + budgetName + "\"");
        msg.setText(String.format(
                "Bạn đã chi %.2f vượt hạn mức %.2f cho ngân sách \"%s\".\nHãy xem xét điều chỉnh kế hoạch chi tiêu.",
                spent != null ? spent.doubleValue() : 0.0,
                amountLimit != null ? amountLimit.doubleValue() : 0.0,
                budgetName
        ));
        mailSender.send(msg);
    }
}
