package com.petshop.petshop.controller;

import com.petshop.petshop.dto.AdminCreateAccountRequest;
import com.petshop.petshop.model.GioiTinh;
import com.petshop.petshop.model.TrangThaiTaiKhoan;
import com.petshop.petshop.service.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute("accounts", adminAccountService.getAllAccounts());
        model.addAttribute("trangThais", TrangThaiTaiKhoan.values());
        return "admin/accounts/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("req", new AdminCreateAccountRequest());
        model.addAttribute("roles", adminAccountService.getAllRoles());
        model.addAttribute("gioiTinhs", GioiTinh.values());
        model.addAttribute("trangThais", TrangThaiTaiKhoan.values());
        return "admin/accounts/form";
    }

    @PostMapping("/create")
    public String createAccount(@ModelAttribute("req") AdminCreateAccountRequest req,
            RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.createAccount(req);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Integer id,
            @RequestParam TrangThaiTaiKhoan trangThai,
            RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.updateStatus(id, trangThai);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}
