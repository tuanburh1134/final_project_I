package com.example.financeapp.ai.dto;

public class UsageInfo {
    private Integer promptTokens;
    private Integer candidatesTokens;
    private Integer totalTokens;

    public UsageInfo() {
    }

    public UsageInfo(Integer promptTokens, Integer candidatesTokens, Integer totalTokens) {
        this.promptTokens = promptTokens;
        this.candidatesTokens = candidatesTokens;
        this.totalTokens = totalTokens;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCandidatesTokens() {
        return candidatesTokens;
    }

    public void setCandidatesTokens(Integer candidatesTokens) {
        this.candidatesTokens = candidatesTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }
}
