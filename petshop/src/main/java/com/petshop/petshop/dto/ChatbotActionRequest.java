package com.petshop.petshop.dto;

import lombok.Data;

@Data
public class ChatbotActionRequest {
    private String type;
    private Integer productId;
    private Integer quantity;
    private String url;
}
