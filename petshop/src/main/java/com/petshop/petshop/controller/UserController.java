/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.controller;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        model.addAttribute("errorAccountLocked", false);
        model.addAttribute("errorAccountDisabled", false);

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object exception = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            if (exception instanceof LockedException) {
                model.addAttribute("errorAccountLocked", true);
            } else if (exception instanceof DisabledException) {
                model.addAttribute("errorAccountDisabled", true);
            }
        }
        // Khởi tạo sớm Token CSRF và Session trước khi Thymeleaf bắt đầu vẽ HTML
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        return "user/login";
    }
}