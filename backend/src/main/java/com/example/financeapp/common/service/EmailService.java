package com.example.financeapp.common.service;

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
    @Autowired
    private JavaMailSender mailSender;

    @Value("${SUPPORT_FEEDBACK_EMAIL:}")
    private String supportFeedbackEmail;

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
