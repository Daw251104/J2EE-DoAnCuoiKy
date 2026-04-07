package com.petshop.petshop.controller;

import com.petshop.petshop.dto.AdminCreateAccountRequest;
import com.petshop.petshop.model.GioiTinh;
import com.petshop.petshop.model.TrangThaiTaiKhoan;
import com.petshop.petshop.service.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    public String listAccounts(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false, name = "trangThai") String trangThaiFilter,
                               Model model) {
        TrangThaiTaiKhoan parsedTrangThai = parseTrangThai(trangThaiFilter);

        model.addAttribute("accounts", adminAccountService.getAccountsByFilters(keyword, role, parsedTrangThai));
        model.addAttribute("roles", adminAccountService.getAllRoles());
        model.addAttribute("trangThais", TrangThaiTaiKhoan.values());
        model.addAttribute("filterKeyword", keyword);
        model.addAttribute("filterRole", role);
        model.addAttribute("filterTrangThai", trangThaiFilter);
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
            redirectAttributes.addFlashAttribute("successMessage", "Tao tai khoan thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Integer id,
                               @RequestParam TrangThaiTaiKhoan trangThai,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false) String trangThaiFilter,
                               RedirectAttributes redirectAttributes) {
        try {
            adminAccountService.updateStatus(id, trangThai);
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat trang thai thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi: " + e.getMessage());
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("role", role);
        redirectAttributes.addAttribute("trangThai", trangThaiFilter);
        return "redirect:/admin/accounts";
    }

    private TrangThaiTaiKhoan parseTrangThai(String rawTrangThai) {
        if (rawTrangThai == null || rawTrangThai.isBlank()) {
            return null;
        }

        try {
            return TrangThaiTaiKhoan.valueOf(rawTrangThai.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
