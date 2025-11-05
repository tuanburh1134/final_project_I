package com.example.financeapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Bổ sung annotation này nếu chưa có
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF cho môi trường phát triển
                .csrf(csrf -> csrf.disable())

                // Cấu hình quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",         // cho phép đăng nhập, đăng ký
                                "/oauth2/**",       // cho phép Google OAuth2
                                "/error"            // cho phép error page
                        ).permitAll()
                        .anyRequest().authenticated() // các request khác yêu cầu xác thực
                )

                // Bật đăng nhập OAuth2 (Google)
                .oauth2Login(oauth2 -> oauth2
                        // URL trả về sau khi login Google thành công
                        .defaultSuccessUrl("/auth/google/success", true)
                )

                // Bật httpBasic để test nhanh với Postman (tùy chọn)
                .httpBasic(httpBasic -> {});

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}