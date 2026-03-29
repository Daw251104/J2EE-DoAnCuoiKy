package com.petshop.petshop.controller;

import com.petshop.petshop.model.SanPham;
import com.petshop.petshop.repository.LoaiSanPhamRepository;
import com.petshop.petshop.repository.LoaiThuCungRepository;
import com.petshop.petshop.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petshop.petshop.dto.SanPhamDTO;
import com.petshop.petshop.service.service.SanPhamService;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/sanpham")
public class SanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private LoaiSanPhamRepository loaiSanPhamRepository;

    @Autowired
    private LoaiThuCungRepository loaiThuCungRepository;

    @Autowired
    private SanPhamService sanPhamService;

    // Xem danh sách sản phẩm (Mọi người đều xem được)
    @GetMapping
    public String listSanPham(Model model) {
        List<SanPham> sanPhams = sanPhamRepository.findAll();
        model.addAttribute("sanPhams", sanPhams);
        return "sanpham/index";
    }

    // Hiển thị form Thêm sản phẩm (Chỉ OWNER/CHU_CUA_HANG)
    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("sanPham", new SanPhamDTO());
        model.addAttribute("loaiSanPhams", loaiSanPhamRepository.findAll());
        model.addAttribute("loaiThuCungs", loaiThuCungRepository.findAll());
        return "sanpham/form";
    }

    // Xử lý Thêm sản phẩm (Chỉ OWNER/CHU_CUA_HANG)
    @PostMapping("/them")
    public String addSanPham(@ModelAttribute("sanPham") SanPhamDTO sanPhamDTO, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.createSanPham(sanPhamDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
        return "redirect:/sanpham";
    }

    // Hiển thị form Sửa sản phẩm (Chỉ OWNER/CHU_CUA_HANG)
    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        SanPham sanPham = sanPhamRepository.findById(id).orElse(null);
        if (sanPham == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
            return "redirect:/sanpham";
        }
        
        SanPhamDTO dto = new SanPhamDTO();
        dto.setMaSP(sanPham.getMaSP());
        dto.setTenSP(sanPham.getTenSP());
        dto.setGiaBan(sanPham.getGiaBan());
        dto.setSlTon(sanPham.getSlTon());
        dto.setKhuyenMai(sanPham.getKhuyenMai());
        dto.setTinhTrang(sanPham.getTinhTrang());
        dto.setMoTa(sanPham.getMoTa());
        if (sanPham.getLoaiSanPham() != null) {
            dto.setMaLoai(sanPham.getLoaiSanPham().getMaLoai());
        }
        if (sanPham.getLoaiThuCung() != null) {
            dto.setMaLoaiTC(sanPham.getLoaiThuCung().getMaLoaiTC());
        }

        model.addAttribute("sanPham", dto);
        model.addAttribute("oldHinhDaiDien", sanPham.getHinhDaiDien());
        
        model.addAttribute("loaiSanPhams", loaiSanPhamRepository.findAll());
        model.addAttribute("loaiThuCungs", loaiThuCungRepository.findAll());
        return "sanpham/form";
    }

    // Xử lý Cập nhật sản phẩm (Chỉ OWNER/CHU_CUA_HANG)
    @PostMapping("/sua/{id}")
    public String editSanPham(@PathVariable("id") Integer id, @ModelAttribute("sanPham") SanPhamDTO sanPhamDTO, RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.updateSanPham(id, sanPhamDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy sản phẩm hoặc thông tin không hợp lệ! " + e.getMessage());
        }
        return "redirect:/sanpham";
    }

    // Xử lý Xóa sản phẩm (Chỉ OWNER/CHU_CUA_HANG)
    @GetMapping("/xoa/{id}")
    public String deleteSanPham(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            sanPhamRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm vì có dữ liệu liên quan!");
        }
        return "redirect:/sanpham";
    }
}
