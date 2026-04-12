package com.petshop.petshop.controller;

import com.petshop.petshop.dto.DonHangResponse;
import com.petshop.petshop.dto.GioHangResponse;
import com.petshop.petshop.dto.ThanhToanRequest;
import com.petshop.petshop.model.DonHang;
import com.petshop.petshop.model.PhuongThucThanhToan;
import com.petshop.petshop.repository.PhuongThucThanhToanRepository;
import com.petshop.petshop.service.service.DonHangService;
import com.petshop.petshop.service.service.GioHangService;
import com.petshop.petshop.service.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class DonHangController {

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private VnPayService vnPayService;

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
     * Xử lý đặt hàng — nếu PTTT là VNPay thì redirect sang cổng VNPay
     */
    @PostMapping("/thanh-toan")
    public String datHang(@ModelAttribute ThanhToanRequest request,
                          Principal principal,
                          HttpServletRequest httpRequest,
                          RedirectAttributes redirectAttributes) {
        try {
            DonHang donHang = donHangService.datHang(principal.getName(), request);

            // Kiểm tra phương thức thanh toán có phải VNPay không
            PhuongThucThanhToan pttt = donHang.getPhuongThucThanhToan();
            if (pttt != null && pttt.getTenLoaiTT() != null
                    && pttt.getTenLoaiTT().toLowerCase().contains("vnpay")) {

                // Lấy IP khách hàng
                String ipAddr = httpRequest.getHeader("X-Forwarded-For");
                if (ipAddr == null || ipAddr.isEmpty()) {
                    ipAddr = httpRequest.getRemoteAddr();
                }

                // Tổng tiền (convert BigDecimal → long)
                long amount = donHang.getTongTien().longValue();

                // Tạo URL VNPay
                String vnpayUrl = vnPayService.generatePaymentUrl(donHang.getMaDH(), amount, ipAddr);

                // Redirect sang cổng VNPay
                return "redirect:" + vnpayUrl;
            }

            // Nếu thanh toán COD (tiền mặt) → về trang đơn hàng như cũ
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt hàng thành công! Mã đơn hàng của bạn là: #" + donHang.getMaDH());
            return "redirect:/don-hang";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi: " + e.getMessage());
            return "redirect:/thanh-toan";
        }
    }

    /**
     * VNPay Return URL — VNPay redirect khách hàng về đây sau khi thanh toán
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params,
                              RedirectAttributes redirectAttributes) {
        // Verify chữ ký từ VNPay
        boolean isValid = vnPayService.validateSignature(params);
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef"); // maDH

        if (isValid && "00".equals(responseCode)) {
            // Thanh toán thành công — cập nhật đơn hàng
            try {
                int maDH = Integer.parseInt(txnRef);
                donHangService.capNhatThanhToanVnPay(maDH, true);
                /*
                if (donHang != null) {
                    donHang.setDaThanhToan(1); // Đánh dấu đã thanh toán
                    donHangRepository.save(donHang);
                }
                */
                redirectAttributes.addFlashAttribute("successMessage",
                        "Thanh toán VNPay thành công! Mã đơn hàng: #" + txnRef);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Thanh toán VNPay thành công! Mã đơn hàng: #" + txnRef);
            }
        } else {
            // Thanh toán thất bại
            try {
                if (txnRef != null && !txnRef.isBlank()) {
                    donHangService.capNhatThanhToanVnPay(Integer.parseInt(txnRef), false);
                }
            } catch (NumberFormatException ignored) {
            }
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Thanh toán VNPay thất bại! Mã lỗi: " + responseCode);
        }

        return "redirect:/don-hang";
    }

    /**
     * Lịch sử đơn hàng (CUSTOMER)
     */
    @GetMapping("/don-hang")
    public String lichSuDonHang(@RequestParam(required = false) String trangThai, Model model, Principal principal) {
        List<DonHangResponse> donHangs = donHangService.layDanhSachDonHang(principal.getName(), trangThai);
        model.addAttribute("donHangs", donHangs);
        model.addAttribute("currentTrangThai", trangThai);
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
    /**
     * Khách hàng hủy đơn hàng (khi ở trạng thái CHO_XAC_NHAN)
     */
    @PostMapping("/don-hang/{id}/huy")
    public String huyDonHang(@PathVariable("id") Integer id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            donHangService.huyDonHang(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id + " thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/don-hang";
    }

    // ========================= STAFF / OWNER =========================

    /**
     * Danh sách tất cả đơn hàng (STAFF/OWNER)
     */
    @GetMapping("/staff/don-hang")
    public String tatCaDonHang(@RequestParam(required = false) String trangThai, Model model) {
        List<DonHangResponse> tatCa = donHangService.layTatCaDonHang(null);
        
        // Tính toán thống kê trên server
        long choXacNhan = tatCa.stream().filter(d -> "CHO_XAC_NHAN".equals(d.getTrangThai())).count();
        long choGiaoHang = tatCa.stream().filter(d -> "CHO_GIAO_HANG".equals(d.getTrangThai())).count();
        long dangGiao = tatCa.stream().filter(d -> "DANG_GIAO".equals(d.getTrangThai())).count();
        long daGiao = tatCa.stream().filter(d -> "DA_GIAO".equals(d.getTrangThai())).count();
        long daHuy = tatCa.stream().filter(d -> "DA_HUY".equals(d.getTrangThai())).count();
        
        List<DonHangResponse> donHangs = donHangService.layTatCaDonHang(trangThai);
        
        model.addAttribute("donHangs", donHangs);
        model.addAttribute("countChoXacNhan", choXacNhan);
        model.addAttribute("countChoGiaoHang", choGiaoHang);
        model.addAttribute("countDangGiao", dangGiao);
        model.addAttribute("countDaGiao", daGiao);
        model.addAttribute("countDaHuy", daHuy);
        model.addAttribute("currentTrangThai", trangThai);
        
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
            redirectAttributes.addFlashAttribute("successMessage", "Da xac nhan don hang #" + maDH + " -> Cho giao hang!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/don-hang";
    }

    @PostMapping("/staff/don-hang/{maDH}/xac-nhan-giao")
    public String xacNhanGiaoHang(@PathVariable Integer maDH,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            donHangService.xacNhanGiaoHang(maDH, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Da chuyen don hang #" + maDH + " sang Dang giao!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lá»—i: " + e.getMessage());
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
