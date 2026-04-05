/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.SanPhamDTO;
import com.petshop.petshop.dto.SanPhamResponseDTO;
import com.petshop.petshop.model.HinhAnhSanPham;
import com.petshop.petshop.model.LoaiSanPham;
import com.petshop.petshop.model.LoaiThuCung;
import com.petshop.petshop.model.SanPham;
import com.petshop.petshop.repository.HinhAnhSanPhamRepository;
import com.petshop.petshop.repository.LoaiSanPhamRepository;
import com.petshop.petshop.repository.LoaiThuCungRepository;
import com.petshop.petshop.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Service
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final LoaiSanPhamRepository loaiSanPhamRepository;
    private final LoaiThuCungRepository loaiThuCungRepository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;

    @Autowired
    public SanPhamService(SanPhamRepository sanPhamRepository,
                          LoaiSanPhamRepository loaiSanPhamRepository,
                          LoaiThuCungRepository loaiThuCungRepository,
                          HinhAnhSanPhamRepository hinhAnhSanPhamRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.loaiSanPhamRepository = loaiSanPhamRepository;
        this.loaiThuCungRepository = loaiThuCungRepository;
        this.hinhAnhSanPhamRepository = hinhAnhSanPhamRepository;
    }

    // Hiển thị danh sách sản phẩm
    public List<SanPhamResponseDTO> getAllSanPhams() {
        return sanPhamRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID
    public SanPhamResponseDTO getSanPhamById(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + id));
        return convertToResponseDTO(sanPham);
    }

    // Thêm sản phẩm mới
    @Transactional
    public SanPhamResponseDTO createSanPham(SanPhamDTO dto) {
        SanPham sanPham = new SanPham();
        mapDtoToEntity(dto, sanPham);
        sanPham.setNgayTao(LocalDateTime.now());
        
        // Xử lý ảnh đại diện
        if (dto.getHinhDaiDien() != null && !dto.getHinhDaiDien().isEmpty()) {
            sanPham.setHinhDaiDien(saveImage(dto.getHinhDaiDien()));
        }

        sanPham = sanPhamRepository.save(sanPham);

        // Thêm hình ảnh nếu có
        if (dto.getHinhAnhs() != null && !dto.getHinhAnhs().isEmpty()) {
            for (MultipartFile file : dto.getHinhAnhs()) {
                if(file != null && !file.isEmpty()) {
                    HinhAnhSanPham ha = new HinhAnhSanPham();
                    ha.setHinhAnh(saveImage(file));
                    ha.setSanPham(sanPham);
                    hinhAnhSanPhamRepository.save(ha);
                }
            }
        }

        return convertToResponseDTO(sanPham);
    }

    // Sửa sản phẩm
    @Transactional
    public SanPhamResponseDTO updateSanPham(Integer id, SanPhamDTO dto) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + id));
        mapDtoToEntity(dto, sanPham);
        
        // Cập nhật ảnh đại diện nếu có
        if (dto.getHinhDaiDien() != null && !dto.getHinhDaiDien().isEmpty()) {
             sanPham.setHinhDaiDien(saveImage(dto.getHinhDaiDien()));
        }
        
        sanPham = sanPhamRepository.save(sanPham);

        // Cập nhật hình ảnh: Xóa cũ và thêm mới (giả sử replace toàn bộ)
        if (dto.getHinhAnhs() != null && !dto.getHinhAnhs().isEmpty()) {
            hinhAnhSanPhamRepository.deleteAll(sanPham.getHinhAnhSanPhams());
            for (MultipartFile file : dto.getHinhAnhs()) {
                 if(file != null && !file.isEmpty()) {
                    HinhAnhSanPham ha = new HinhAnhSanPham();
                    ha.setHinhAnh(saveImage(file));
                    ha.setSanPham(sanPham);
                    hinhAnhSanPhamRepository.save(ha);
                 }
            }
        }

        return convertToResponseDTO(sanPham);
    }

    // Xóa sản phẩm
    @Transactional
    public void deleteSanPham(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + id));
        // Xóa hình ảnh liên quan trước
        hinhAnhSanPhamRepository.deleteAll(sanPham.getHinhAnhSanPhams());
        sanPhamRepository.delete(sanPham);
    }

    private void mapDtoToEntity(SanPhamDTO dto, SanPham sanPham) {
        sanPham.setTenSP(dto.getTenSP());
        sanPham.setGiaBan(dto.getGiaBan());
        sanPham.setSlTon(dto.getSlTon());
        sanPham.setKhuyenMai(dto.getKhuyenMai());
        sanPham.setTinhTrang(dto.getTinhTrang());
        sanPham.setMoTa(dto.getMoTa());
        // hinhDaiDien is handled separately

        LoaiSanPham loaiSanPham = loaiSanPhamRepository.findById(dto.getMaLoai())
                .orElseThrow(() -> new RuntimeException("Loại sản phẩm không tồn tại với ID: " + dto.getMaLoai()));
        sanPham.setLoaiSanPham(loaiSanPham);

        LoaiThuCung loaiThuCung = loaiThuCungRepository.findById(dto.getMaLoaiTC())
                .orElseThrow(() -> new RuntimeException("Loại thú cưng không tồn tại với ID: " + dto.getMaLoaiTC()));
        sanPham.setLoaiThuCung(loaiThuCung);
    }

    private SanPhamResponseDTO convertToResponseDTO(SanPham sanPham) {
        SanPhamResponseDTO response = new SanPhamResponseDTO();
        response.setMaSP(sanPham.getMaSP());
        response.setTenSP(sanPham.getTenSP());
        response.setGiaBan(sanPham.getGiaBan());
        response.setSlTon(sanPham.getSlTon());
        response.setKhuyenMai(sanPham.getKhuyenMai());
        response.setTinhTrang(sanPham.getTinhTrang());
        response.setMoTa(sanPham.getMoTa());
        response.setHinhDaiDien(sanPham.getHinhDaiDien());
        response.setNgayTao(sanPham.getNgayTao());
        response.setTenLoaiSanPham(sanPham.getLoaiSanPham() != null ? sanPham.getLoaiSanPham().getTenLoai() : null);
        response.setTenLoaiThuCung(sanPham.getLoaiThuCung() != null ? sanPham.getLoaiThuCung().getTenLoaiTC() : null);
        if (sanPham.getHinhAnhSanPhams() != null) {
            response.setHinhAnhs(sanPham.getHinhAnhSanPhams().stream()
                    .map(HinhAnhSanPham::getHinhAnh)
                    .collect(Collectors.toList()));
        } else {
            response.setHinhAnhs(List.of()); // Trả về danh sách rỗng nếu không có hình
        }
        return response;
    }

    private String saveImage(MultipartFile file) {
        try {
            String projectRoot = System.getProperty("user.dir");
            String relativePath = "/uploads/products/";
            String uploadDir = projectRoot + relativePath;

            Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadDirPath);

            String fileName = "product_" + System.currentTimeMillis() + "_" 
                              + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");

            Path uploadPath = uploadDirPath.resolve(fileName);
            file.transferTo(uploadPath.toFile());

            return "/uploads/products/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu hình ảnh sản phẩm: " + e.getMessage(), e);
        }
    }
}