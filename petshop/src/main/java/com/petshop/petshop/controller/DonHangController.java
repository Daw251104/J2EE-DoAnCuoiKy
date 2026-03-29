package com.petshop.petshop.controller;

import com.petshop.petshop.dto.DonHangResponse;
import com.petshop.petshop.dto.GioHangResponse;
import com.petshop.petshop.dto.ThanhToanRequest;
import com.petshop.petshop.model.DonHang;
import com.petshop.petshop.model.PhuongThucThanhToan;
import com.petshop.petshop.repository.PhuongThucThanhToanRepository;
import com.petshop.petshop.service.service.DonHangService;
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
public class DonHangController {

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    /**
     * Hiển thị trang form thanh toán
     */
    @GetMapping("/thanh-toan")
    public String hienThiThanhToan(Model model, Principal principal) {
        List<GioHangResponse> gioHangItems = gioHangService.layGioHang(principal.getName());
        if (gioHangItems.isEmpty()) {
            return "redirect:/gio-hang";
        }
        BigDecimal tongTien = gioHangService.tinhTongTien(gioHangItems);
        List<PhuongThucThanhToan> dsPTTT = phuongThucThanhToanRepository.findAll();

        model.addAttribute("gioHangItems", gioHangItems);
        model.addAttribute("tongTien", tongTien);
        model.addAttribute("dsPTTT", dsPTTT);
        model.addAttribute("thanhToanRequest", new ThanhToanRequest());
        return "donhang/thanh-toan";
    }

    /**
     * Xử lý đặt hàng
     */
    @PostMapping("/thanh-toan")
    public String datHang(@ModelAttribute ThanhToanRequest request,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            DonHang donHang = donHangService.datHang(principal.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt hàng thành công! Mã đơn hàng của bạn là: #" + donHang.getMaDH());
            return "redirect:/don-hang";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/thanh-toan";
        }
    }

    /**
     * Lịch sử đơn hàng (CUSTOMER)
     */
    @GetMapping("/don-hang")
    public String lichSuDonHang(Model model, Principal principal) {
        List<DonHangResponse> donHangs = donHangService.layDanhSachDonHang(principal.getName());
        model.addAttribute("donHangs", donHangs);
        return "donhang/lich-su";
    }

    /**
     * Chi tiết đơn hàng (CUSTOMER)
     */
    @GetMapping("/don-hang/{maDH}")
    public String chiTietDonHang(@PathVariable Integer maDH,
                                 Model model,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            DonHangResponse donHang = donHangService.layChiTietDonHang(maDH, principal.getName());
            model.addAttribute("donHang", donHang);
            return "donhang/chi-tiet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/don-hang";
        }
    }

    /**
     * Khách hàng xác nhận đã nhận hàng
     */
    @PostMapping("/don-hang/{maDH}/xac-nhan-nhan-hang")
    public String xacNhanNhanHang(@PathVariable Integer maDH,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            donHangService.xacNhanDaGiao(maDH, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã xác nhận nhận hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/don-hang/" + maDH;
    }

    // ========================= STAFF / OWNER =========================

    /**
     * Danh sách tất cả đơn hàng (STAFF/OWNER)
     */
    @GetMapping("/staff/don-hang")
    public String tatCaDonHang(Model model) {
        List<DonHangResponse> donHangs = donHangService.layTatCaDonHang();
        
        // Tính toán thống kê trên server
        long choXacNhan = donHangs.stream().filter(d -> "CHO_XAC_NHAN".equals(d.getTrangThai())).count();
        long dangGiao = donHangs.stream().filter(d -> "DANG_GIAO".equals(d.getTrangThai())).count();
        long daGiao = donHangs.stream().filter(d -> "DA_GIAO".equals(d.getTrangThai())).count();
        long daHuy = donHangs.stream().filter(d -> "DA_HUY".equals(d.getTrangThai())).count();
        
        model.addAttribute("donHangs", donHangs);
        model.addAttribute("countChoXacNhan", choXacNhan);
        model.addAttribute("countDangGiao", dangGiao);
        model.addAttribute("countDaGiao", daGiao);
        model.addAttribute("countDaHuy", daHuy);
        
        return "donhang/staff-list";
    }

    /**
     * Chi tiết đơn hàng cho staff
     */
    @GetMapping("/staff/don-hang/{maDH}")
    public String chiTietDonHangStaff(@PathVariable Integer maDH,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            DonHangResponse donHang = donHangService.layChiTietDonHangStaff(maDH);
            model.addAttribute("donHang", donHang);
            return "donhang/staff-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/staff/don-hang";
        }
    }

    /**
     * Xác nhận đơn hàng (STAFF/OWNER)
     */
    @PostMapping("/staff/don-hang/{maDH}/xac-nhan")
    public String xacNhanDonHang(@PathVariable Integer maDH,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            donHangService.xacNhanDonHang(maDH, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn hàng #" + maDH + " → Đang giao!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/don-hang";
    }

    /**
     * Từ chối đơn hàng (STAFF/OWNER)
     */
    @PostMapping("/staff/don-hang/{maDH}/tu-choi")
    public String tuChoiDonHang(@PathVariable Integer maDH,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            donHangService.tuChoiDonHang(maDH, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đơn hàng #" + maDH + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/don-hang";
    }
}
