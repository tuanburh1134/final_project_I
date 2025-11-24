package com.example.financeapp.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalTime;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@financeapp.com}")
    private String defaultFrom;

    @Value("${app.mail.mock:false}")
    private boolean mockMode;

    // Hàm gửi chung
    private void send(String to, String subject, String content) {
        if (mockMode) {
            log.info("[MOCK EMAIL] To: {}\nSubject: {}\nContent:\n{}", to, subject, content);
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(defaultFrom);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(content);
            mailSender.send(msg);

            log.info("Đã gửi email tới {}", to);
        } catch (Exception ex) {
            log.error("Gửi email thất bại tới " + to, ex);
        }
    }

    // Đăng ký (giữ lại để tương thích)
    public void sendRegistrationVerificationEmail(String to, String code) {
        sendOtpRegisterEmail(to, code);
    }

    // Khôi phục mật khẩu (giữ lại để tương thích)
    public void sendPasswordResetEmail(String to, String code) {
        sendOtpResetPasswordEmail(to, code);
    }

    // Gửi OTP đăng ký
    public void sendOtpRegisterEmail(String email, String otp) {
        String subject = "[FinanceApp] Mã xác thực đăng ký tài khoản";
        String content = "Xin chào,\n\n"
                + "Mã OTP đăng ký tài khoản của bạn là: " + otp + "\n"
                + "Mã có hiệu lực trong 5 phút.\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.\n\n"
                + "Trân trọng,\nĐội ngũ FinanceApp";
        send(email, subject, content);
    }

    // Gửi OTP quên mật khẩu
    public void sendOtpResetPasswordEmail(String email, String otp) {
        String subject = "[FinanceApp] Mã xác thực đặt lại mật khẩu";
        String content = "Xin chào,\n\n"
                + "Mã OTP đặt lại mật khẩu của bạn là: " + otp + "\n"
                + "Mã có hiệu lực trong 5 phút.\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, vui lòng đổi mật khẩu hoặc liên hệ hỗ trợ.\n\n"
                + "Trân trọng,\nĐội ngũ FinanceApp";
        send(email, subject, content);
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

    public void sendDailyReminderEmail(String to, LocalTime reminderTime) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Nhắc ghi chép thu chi");
        msg.setText(String.format(
                "Đã đến giờ %s. Đừng quên ghi lại các giao dịch trong ngày để quản lý tài chính hiệu quả!",
                reminderTime
        ));
        mailSender.send(msg);
    }

    public void sendScheduledTransactionFailureEmail(String to, String walletName,
                                                      BigDecimal amount, String reason) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("⚠️ Giao dịch định kỳ thất bại");
        msg.setText(String.format(
                "Giao dịch định kỳ %.2f ở ví \"%s\" không thể thực hiện.\nLý do: %s\n" +
                        "Hệ thống sẽ thử lại vào lần chạy tiếp theo nếu lịch vẫn còn hiệu lực.",
                amount != null ? amount.doubleValue() : 0.0,
                walletName != null ? walletName : "Ví",
                reason != null ? reason : "Không xác định"
        ));
        mailSender.send(msg);
    }

    public void sendFeedbackNotification(String userFullName,
                                         String contactEmail,
                                         String title,
                                         String description,
                                         String module,
                                         String platform,
                                         String priority) {
        String recipient = StringUtils.hasText(supportFeedbackEmail)
                ? supportFeedbackEmail
                : contactEmail;
        if (!StringUtils.hasText(recipient)) {
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(recipient);
        msg.setSubject("[Feedback] " + title);
        msg.setText(String.format(
                """
                Người dùng: %s (%s)
                Ưu tiên: %s
                Màn hình: %s
                Nền tảng/Phiên bản: %s

                Nội dung:
                %s
                """,
                userFullName != null ? userFullName : "Không rõ",
                contactEmail != null ? contactEmail : "N/A",
                priority,
                module != null ? module : "Chưa cung cấp",
                platform != null ? platform : "Chưa cung cấp",
                description
        ));
        mailSender.send(msg);
    }
}
