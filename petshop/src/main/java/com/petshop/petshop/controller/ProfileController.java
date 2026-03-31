package com.petshop.petshop.controller;

import com.petshop.petshop.dto.UpdateProfileRequest;
import com.petshop.petshop.model.TaiKhoan;
import com.petshop.petshop.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        String username = principal.getName();
        TaiKhoan taiKhoan = userService.layThongTinTaiKhoan(username);
        
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setHoTen(taiKhoan.getHoTen());
        request.setSdt(taiKhoan.getSdt());
        request.setGioiTinh(taiKhoan.getGioiTinh());
        request.setDiaChi(taiKhoan.getDiaChi());
        request.setEmail(taiKhoan.getEmail());
        request.setAnhDaiDien(taiKhoan.getAnhDaiDien());

        model.addAttribute("taiKhoan", taiKhoan);
        model.addAttribute("updateProfileRequest", request);
        model.addAttribute("gioiTinhs", com.petshop.petshop.model.GioiTinh.values());
        
        return "profile/index";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UpdateProfileRequest request, 
                                Principal principal, 
                                RedirectAttributes redirectAttributes) {
        try {
            userService.capNhatThongTin(principal.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
