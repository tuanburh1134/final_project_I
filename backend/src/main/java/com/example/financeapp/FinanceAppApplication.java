package com.example.financeapp;

import com.example.financeapp.security.Role;
import com.example.financeapp.user.entity.User;
import com.example.financeapp.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication
public class FinanceAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceAppApplication.class, args);
    }

    /**
     * Tạo ADMIN mặc định khi chạy lần đầu
     */
    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {

            String adminEmail = "tranvinhtri2705@gmail.com";

            if (!userRepository.existsByEmail(adminEmail)) {

                User admin = User.builder()
                        .email(adminEmail)
                        .fullName("System Administrator")
                        .password(encoder.encode("Admin@123"))  // mật khẩu mặc định
                        .role(Role.ADMIN)
                        .locked(false)
                        .googleAccount(false)
                        .firstLogin(false)
                        .build();

                userRepository.save(admin);

                System.out.println("====> ADMIN CREATED: " + adminEmail + " / Admin@123");
            } else {
                System.out.println("====> ADMIN ALREADY EXISTS, SKIP");
            }
        };
    }
}
