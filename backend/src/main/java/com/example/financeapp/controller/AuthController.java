package com.example.financeapp.controller;

import com.example.financeapp.config.JwtUtil;
import com.example.financeapp.dto.LoginRequest;
import com.example.financeapp.entity.User;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.service.EmailService;
import com.example.financeapp.service.RecaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RecaptchaService recaptchaService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // -----------------------------
    // 📌 ĐĂNG KÝ (có CAPTCHA + gửi mã email)
    // -----------------------------
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        Map<String, Object> res = new HashMap<>();

        String fullName = request.get("fullName");
        String email = request.get("email");
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");
        String recaptchaToken = request.get("recaptchaToken");

        if (fullName == null || email == null || password == null || confirmPassword == null || recaptchaToken == null) {
            res.put("error", "Thiếu thông tin đăng ký hoặc CAPTCHA (vui lòng gửi fullName, email, password, confirmPassword, recaptchaToken)");
            return res;
        }

        // Kiểm tra password confirm
        if (!password.equals(confirmPassword)) {
            res.put("error", "Mật khẩu và xác nhận mật khẩu không khớp");
            return res;
        }

        // ✅ Kiểm tra độ mạnh mật khẩu
        if (!isStrongPassword(password)) {
            res.put("error", "Mật khẩu phải tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            return res;
        }

        // ✅ Kiểm tra CAPTCHA
        if (!recaptchaService.verifyToken(recaptchaToken)) {
            res.put("error", "CAPTCHA không hợp lệ");
            return res;
        }

        // ✅ Kiểm tra email trùng
        if (userRepository.existsByEmail(email)) {
            res.put("error", "Email đã được sử dụng");
            return res;
        }

        // ✅ Tạo mã xác minh 6 chữ số
        String verificationCode = String.format("%06d", new Random().nextInt(1_000_000));

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setProvider("local");
        newUser.setEnabled(false);
        newUser.setVerificationCode(verificationCode);
        userRepository.save(newUser);

        // ✅ Gửi mã xác nhận về email
        emailService.sendVerificationEmail(email, verificationCode);

        res.put("message", "Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.");
        return res;
    }

    // -----------------------------
    // 📩 XÁC MINH EMAIL
    // -----------------------------
    @PostMapping("/verify")
    public Map<String, Object> verifyAccount(@RequestBody Map<String, String> request) {
        Map<String, Object> res = new HashMap<>();

        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            res.put("error", "Thiếu email hoặc mã xác minh");
            return res;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            res.put("error", "Tài khoản không tồn tại");
            return res;
        }

        User user = userOpt.get();

        if (user.isEnabled()) {
            res.put("message", "Tài khoản đã được kích hoạt trước đó");
            return res;
        }

        if (code.equals(user.getVerificationCode())) {
            user.setEnabled(true);
            user.setVerificationCode(null);
            userRepository.save(user);

            String accessToken = jwtUtil.generateToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            res.put("message", "Xác minh thành công");
            res.put("accessToken", accessToken);
            res.put("refreshToken", refreshToken);
            return res;
        } else {
            res.put("error", "Mã xác minh không đúng");
            return res;
        }
    }

    // -----------------------------
    // 📌 ĐĂNG NHẬP (chỉ cho tài khoản đã xác minh)
    // -----------------------------
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> res = new HashMap<>();

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            res.put("error", "Email không tồn tại");
            return res;
        }

        User user = userOpt.get();

        if (!user.isEnabled()) {
            res.put("error", "Tài khoản chưa được xác minh. Vui lòng kiểm tra email.");
            return res;
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            res.put("error", "Sai mật khẩu");
            return res;
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        res.put("message", "Đăng nhập thành công");
        res.put("accessToken", accessToken);
        res.put("refreshToken", refreshToken);
        res.put("user", user);
        return res;
    }

    // -----------------------------
    // 🔑 ĐỔI MẬT KHẨU (YÊU CẦU MẠNH)
    // -----------------------------
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> request) {
        Map<String, Object> res = new HashMap<>();

        String email = request.get("email");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (email == null || oldPassword == null || newPassword == null) {
            res.put("error", "Thiếu thông tin (email, mật khẩu cũ, mật khẩu mới)");
            return res;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            res.put("error", "Tài khoản không tồn tại");
            return res;
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            res.put("error", "Mật khẩu cũ không đúng");
            return res;
        }

        if (!isStrongPassword(newPassword)) {
            res.put("error", "Mật khẩu mới phải tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            return res;
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            res.put("error", "Mật khẩu mới không được trùng với mật khẩu cũ");
            return res;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        res.put("message", "Đổi mật khẩu thành công");
        return res;
    }

    // -----------------------------
    // 🔄 LÀM MỚI TOKEN
    // -----------------------------
    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(@RequestBody Map<String, String> request) {
        Map<String, Object> res = new HashMap<>();

        try {
            String refreshToken = request.get("refreshToken");
            String email = jwtUtil.extractEmail(refreshToken);

            if (jwtUtil.validateToken(refreshToken, email)) {
                String newAccessToken = jwtUtil.generateToken(email);
                res.put("accessToken", newAccessToken);
                res.put("message", "Làm mới token thành công");
            } else {
                res.put("error", "Refresh token không hợp lệ hoặc đã hết hạn");
            }
        } catch (Exception e) {
            res.put("error", "Refresh token không hợp lệ");
        }

        return res;
    }

    // -----------------------------
    // 🚪 ĐĂNG XUẤT
    // -----------------------------
    @PostMapping("/logout")
    public Map<String, String> logout() {
        Map<String, String> res = new HashMap<>();
        res.put("message", "Đăng xuất thành công (xóa token ở client)");
        return res;
    }

    // -----------------------------
    // 🔍 HÀM KIỂM TRA ĐỘ MẠNH MẬT KHẨU
    // -----------------------------
    private boolean isStrongPassword(String password) {
        // Regex: ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
    }
}
