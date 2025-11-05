package com.example.financeapp.service.impl;

import com.example.financeapp.dto.CaptchaResponse;
import com.example.financeapp.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    @Value("${recaptcha.secret.key}")
    private String secretKey;

    @Value("${recaptcha.verify.url}")
    private String verifyUrl;

    private final WebClient webClient;

    public CaptchaServiceImpl() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public boolean verifyCaptcha(String captchaToken) {
        if (captchaToken == null || captchaToken.isEmpty()) {
            log.warn("CAPTCHA token is null or empty");
            return false;
        }

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("secret", secretKey);
            formData.add("response", captchaToken);

            CaptchaResponse response = webClient.post()
                    .uri(verifyUrl)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(CaptchaResponse.class)
                    .block();

            if (response != null && response.isSuccess()) {
                log.info("CAPTCHA verification successful");
                return true;
            } else {
                log.warn("CAPTCHA verification failed: {}", 
                    response != null ? response.getErrorCodes() : "null response");
                return false;
            }
        } catch (Exception e) {
            log.error("Error verifying CAPTCHA: {}", e.getMessage());
            return false;
        }
    }
}

