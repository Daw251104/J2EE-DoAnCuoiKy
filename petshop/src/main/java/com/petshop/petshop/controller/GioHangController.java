package com.petshop.petshop.controller;

import com.petshop.petshop.dto.GioHangRequest;
import com.petshop.petshop.dto.GioHangResponse;
import com.petshop.petshop.service.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/gio-hang")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    /**
     * Hiển thị trang giỏ hàng
     */
    @GetMapping
    public String xemGioHang(Model model, Principal principal) {
        List<GioHangResponse> items = gioHangService.layGioHang(principal.getName());
        BigDecimal tongTien = gioHangService.tinhTongTien(items);
        model.addAttribute("gioHangItems", items);
        model.addAttribute("tongTien", tongTien);
        return "giohang/index";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/them")
    public String themVaoGioHang(@RequestParam Integer maSP,
                                 @RequestParam(defaultValue = "1") Integer soLuong,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            GioHangRequest request = new GioHangRequest();
            request.setMaSP(maSP);
            request.setSoLuong(soLuong);
            gioHangService.themVaoGioHang(principal.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/gio-hang";
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    @PostMapping("/cap-nhat/{maGH}")
    public String capNhatSoLuong(@PathVariable Integer maGH,
                                 @RequestParam Integer soLuong,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            gioHangService.capNhatSoLuong(maGH, soLuong, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/gio-hang";
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @PostMapping("/xoa/{maGH}")
    public String xoaKhoiGioHang(@PathVariable Integer maGH,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            gioHangService.xoaKhoiGioHang(maGH, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/gio-hang";
    }
}
