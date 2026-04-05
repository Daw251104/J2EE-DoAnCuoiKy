package com.petshop.petshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    private String reply;
    private List<ChatbotAction> actions;

    public ChatbotResponse(String reply) {
        this.reply = reply;
        this.actions = List.of();
    }
}
