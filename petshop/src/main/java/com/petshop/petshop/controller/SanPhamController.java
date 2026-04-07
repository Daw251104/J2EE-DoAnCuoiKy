package com.petshop.petshop.controller;

import com.petshop.petshop.dto.SanPhamDTO;
import com.petshop.petshop.model.HinhAnhSanPham;
import com.petshop.petshop.model.LoaiSanPham;
import com.petshop.petshop.model.LoaiThuCung;
import com.petshop.petshop.model.SanPham;
import com.petshop.petshop.repository.LoaiSanPhamRepository;
import com.petshop.petshop.repository.LoaiThuCungRepository;
import com.petshop.petshop.repository.SanPhamRepository;
import com.petshop.petshop.service.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping
    public String listSanPham(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(required = false) Integer maLoaiTC,
                              @RequestParam(required = false) Integer maLoai,
                              @RequestParam(required = false) String tuKhoa,
                              @RequestParam(required = false) String chuCai,
                              Model model) {
        int pageNumber = page < 1 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(pageNumber, 10);
        String normalizedTuKhoa = normalizeKeywordFilter(tuKhoa);
        String normalizedChuCai = normalizeAlphabetFilter(chuCai);

        Page<SanPham> sanPhamPage = sanPhamRepository.locSanPham(
                maLoaiTC, maLoai, normalizedTuKhoa, normalizedChuCai, pageable);

        model.addAttribute("sanPhams", sanPhamPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sanPhamPage.getTotalPages());
        model.addAttribute("totalItems", sanPhamPage.getTotalElements());

        model.addAttribute("loaiSanPhams", loaiSanPhamRepository.findAll());
        model.addAttribute("loaiThuCungs", loaiThuCungRepository.findAll());
        model.addAttribute("currentMaLoaiTC", maLoaiTC);
        model.addAttribute("currentMaLoai", maLoai);
        model.addAttribute("currentTuKhoa", normalizedTuKhoa);
        model.addAttribute("currentChuCai", normalizedChuCai);
        model.addAttribute("alphabetOptions", buildAlphabetOptions());

        return "sanpham/index";
    }

    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("sanPham", new SanPhamDTO());
        model.addAttribute("loaiSanPhams", loaiSanPhamRepository.findAll());
        model.addAttribute("loaiThuCungs", loaiThuCungRepository.findAll());
        return "sanpham/form";
    }

    @PostMapping("/them")
    public String addSanPham(@ModelAttribute("sanPham") SanPhamDTO sanPhamDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.createSanPham(sanPhamDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Them san pham thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi khi them san pham: " + e.getMessage());
        }
        return "redirect:/sanpham";
    }

    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable("id") Integer id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        SanPham sanPham = sanPhamRepository.findById(id).orElse(null);
        if (sanPham == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay san pham!");
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
        dto.setXoaHinhAnhIds(List.of());

        model.addAttribute("sanPham", dto);
        model.addAttribute("oldHinhDaiDien", sanPham.getHinhDaiDien());

        List<HinhAnhSanPham> oldHinhAnhs = sanPham.getHinhAnhSanPhams() == null
                ? List.of()
                : sanPham.getHinhAnhSanPhams().stream()
                .sorted(Comparator.comparing(HinhAnhSanPham::getMaHinhAnh, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        model.addAttribute("oldHinhAnhs", oldHinhAnhs);

        model.addAttribute("loaiSanPhams", loaiSanPhamRepository.findAll());
        model.addAttribute("loaiThuCungs", loaiThuCungRepository.findAll());
        return "sanpham/form";
    }

    @PostMapping("/sua/{id}")
    public String editSanPham(@PathVariable("id") Integer id,
                              @ModelAttribute("sanPham") SanPhamDTO sanPhamDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            sanPhamService.updateSanPham(id, sanPhamDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat san pham thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loi cap nhat san pham: " + e.getMessage());
        }
        return "redirect:/sanpham";
    }

    @GetMapping("/xoa/{id}")
    public String deleteSanPham(@PathVariable("id") Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            sanPhamRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xoa san pham thanh cong!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong the xoa san pham vi co du lieu lien quan!");
        }
        return "redirect:/sanpham";
    }

    @GetMapping("/{id}/chi-tiet")
    @ResponseBody
    public ResponseEntity<?> getSanPhamDetail(@PathVariable("id") Integer id) {
        try {
            return ResponseEntity.ok(sanPhamService.getSanPhamById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/anh-phu/{maHinhAnh}/xoa")
    @ResponseBody
    public ResponseEntity<?> deleteSubImageNow(@PathVariable("id") Integer id,
                                               @PathVariable("maHinhAnh") Integer maHinhAnh) {
        try {
            sanPhamService.deleteSubImage(id, maHinhAnh);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/loai/them")
    @ResponseBody
    public ResponseEntity<?> quickAddLoaiSP(@RequestBody Map<String, String> payload) {
        String tenLoai = payload.get("tenLoai");
        if (tenLoai == null || tenLoai.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Ten loai khong duoc de trong");
        }
        LoaiSanPham loai = new LoaiSanPham();
        loai.setTenLoai(tenLoai);
        loai = loaiSanPhamRepository.save(loai);
        return ResponseEntity.ok(loai);
    }

    @PostMapping("/loaitc/them")
    @ResponseBody
    public ResponseEntity<?> quickAddLoaiTC(@RequestBody Map<String, String> payload) {
        String tenLoaiTC = payload.get("tenLoaiTC");
        if (tenLoaiTC == null || tenLoaiTC.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Ten loai thu cung khong duoc de trong");
        }
        LoaiThuCung loai = new LoaiThuCung();
        loai.setTenLoaiTC(tenLoaiTC);
        loai = loaiThuCungRepository.save(loai);
        return ResponseEntity.ok(loai);
    }

    private String normalizeAlphabetFilter(String chuCai) {
        if (chuCai == null || chuCai.isBlank()) {
            return null;
        }
        String normalized = chuCai.trim().toUpperCase(Locale.ROOT);
        char c = normalized.charAt(0);
        if (c >= 'A' && c <= 'Z') {
            return String.valueOf(c);
        }
        return null;
    }

    private String normalizeKeywordFilter(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return null;
        }
        return tuKhoa.trim();
    }

    private List<String> buildAlphabetOptions() {
        List<String> options = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            options.add(String.valueOf(c));
        }
        return options;
    }
}
