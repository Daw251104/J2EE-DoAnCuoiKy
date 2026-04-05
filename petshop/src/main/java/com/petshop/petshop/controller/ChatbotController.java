package com.petshop.petshop.controller;

import com.petshop.petshop.dto.ChatbotActionRequest;
import com.petshop.petshop.dto.ChatbotRequest;
import com.petshop.petshop.dto.ChatbotResponse;
import com.petshop.petshop.service.ai.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping(
            value = "/ask",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChatbotResponse> ask(@RequestBody(required = false) ChatbotRequest request,
                                               Principal principal) {
        String message = request != null ? request.getMessage() : null;
        String username = principal != null ? principal.getName() : null;
        ChatbotResponse response = chatbotService.ask(message, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/action",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChatbotResponse> action(@RequestBody(required = false) ChatbotActionRequest request,
                                                  Principal principal) {
        String username = principal != null ? principal.getName() : null;
        ChatbotResponse response = chatbotService.executeAction(request, username);
        return ResponseEntity.ok(response);
    }
}
