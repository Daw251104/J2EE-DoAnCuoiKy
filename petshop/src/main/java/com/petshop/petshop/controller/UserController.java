/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.controller;

import com.petshop.petshop.dto.ForgotPasswordRequest;
import com.petshop.petshop.service.service.UserService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        if (!model.containsAttribute("forgotPasswordRequest")) {
            model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        }
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@ModelAttribute ForgotPasswordRequest request,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.datLaiMatKhauTheoUsernameVaEmail(
                    request.getUsername(),
                    request.getEmail(),
                    request.getNewPassword(),
                    request.getConfirmPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập lại.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("forgotPasswordRequest", request);
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "user/forgot-password";
        }
    }
}
