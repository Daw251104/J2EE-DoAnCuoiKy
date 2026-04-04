package com.petshop.petshop.controller;

import com.petshop.petshop.model.LoaiSanPham;
import com.petshop.petshop.model.LoaiThuCung;
import com.petshop.petshop.model.SanPham;
import com.petshop.petshop.repository.LoaiSanPhamRepository;
import com.petshop.petshop.repository.LoaiThuCungRepository;
import com.petshop.petshop.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

import com.petshop.petshop.dto.SanPhamDTO;
import com.petshop.petshop.service.service.SanPhamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

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
    public String listSanPham(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(required = false) Integer maLoaiTC,
                              @RequestParam(required = false) Integer maLoai,
                              Model model) {
        int pageNumber = page < 1 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        
        // filter san pham
        Page<SanPham> sanPhamPage = sanPhamRepository.locSanPham(maLoaiTC, maLoai, pageable);
        
        model.addAttribute("sanPhams", sanPhamPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sanPhamPage.getTotalPages());
        model.addAttribute("totalItems", sanPhamPage.getTotalElements());
        
        // Pass data for filter form
        model.addAttribute("loaiSanPhams", loaiSanPhamRepository.findAll());
        model.addAttribute("loaiThuCungs", loaiThuCungRepository.findAll());
        model.addAttribute("currentMaLoaiTC", maLoaiTC);
        model.addAttribute("currentMaLoai", maLoai);
        
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

    // API thêm nhanh Loại Sản Phẩm
    @PostMapping("/loai/them")
    @ResponseBody
    public ResponseEntity<?> quickAddLoaiSP(@RequestBody Map<String, String> payload) {
        String tenLoai = payload.get("tenLoai");
        if (tenLoai == null || tenLoai.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên loại không được để trống");
        }
        LoaiSanPham loai = new LoaiSanPham();
        loai.setTenLoai(tenLoai);
        loai = loaiSanPhamRepository.save(loai);
        return ResponseEntity.ok(loai);
    }

    // API thêm nhanh Loại Thú Cưng
    @PostMapping("/loaitc/them")
    @ResponseBody
    public ResponseEntity<?> quickAddLoaiTC(@RequestBody Map<String, String> payload) {
        String tenLoaiTC = payload.get("tenLoaiTC");
        if (tenLoaiTC == null || tenLoaiTC.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên loại thú cưng không được để trống");
        }
        LoaiThuCung loai = new LoaiThuCung();
        loai.setTenLoaiTC(tenLoaiTC);
        loai = loaiThuCungRepository.save(loai);
        return ResponseEntity.ok(loai);
    }
}
