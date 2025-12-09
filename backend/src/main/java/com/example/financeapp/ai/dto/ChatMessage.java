package com.example.financeapp.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatMessage {
    @NotBlank
    private String sender;

    @NotBlank
    private String text;

    public ChatMessage() {
    }

    public ChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
