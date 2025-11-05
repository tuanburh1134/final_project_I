package com.example.financeapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CaptchaResponse {
    
    private boolean success;
    
    @JsonProperty("challenge_ts")
    private LocalDateTime challengeTs;
    
    private String hostname;
    
    @JsonProperty("error-codes")
    private List<String> errorCodes;
}

