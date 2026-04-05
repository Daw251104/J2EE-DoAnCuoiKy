package com.petshop.petshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAction {
    private String type;
    private String label;
    private Integer productId;
    private Integer quantity;
    private String url;
}
