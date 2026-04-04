/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.controller;

import com.petshop.petshop.dto.RegisterRequest;
import com.petshop.petshop.dto.TaiKhoanResponse;
import com.petshop.petshop.service.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {
    private final AuthService authService;

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    // Hiển thị form đăng ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("successMessage", null);
        model.addAttribute("errorMessage", null);
        return "user/register"; // tên template: src/main/resources/templates/register.html
    }

    // Xử lý submit form đăng ký (multipart vì có file ảnh)
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "anhDaiDienFile", required = false) MultipartFile anhDaiDienFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Gán file vào DTO (vì RegisterRequest có field MultipartFile)
        request.setAnhDaiDien(anhDaiDienFile);

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin!");
            return "user/register";
        }

        try {
            TaiKhoanResponse response = authService.register(request);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Tài khoản: " + response.getUsername());

            return "user/login"; // redirect để tránh resubmit form
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/register";
        }
    }
}
