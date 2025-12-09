package com.example.financeapp.ai.dto;

public class ChatResponse {
    private String reply;
    private UsageInfo usage;

    public ChatResponse() {
    }

    public ChatResponse(String reply, UsageInfo usage) {
        this.reply = reply;
        this.usage = usage;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public UsageInfo getUsage() {
        return usage;
    }

    public void setUsage(UsageInfo usage) {
        this.usage = usage;
    }
}
