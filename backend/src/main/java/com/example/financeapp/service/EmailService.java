package com.example.financeapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã xác nhận đăng ký tài khoản");
        message.setText("Mã xác nhận của bạn là: " + code + "\n\n"
                + "Nếu bạn không yêu cầu mã này, hãy bỏ qua email này.");
        mailSender.send(message);
    }
}
