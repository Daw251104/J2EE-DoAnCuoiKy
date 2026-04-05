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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<SanPhamResponseDTO> getAllSanPhams() {
        return sanPhamRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public SanPhamResponseDTO getSanPhamById(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("San pham khong ton tai voi ID: " + id));
        return convertToResponseDTO(sanPham);
    }

    @Transactional
    public SanPhamResponseDTO createSanPham(SanPhamDTO dto) {
        SanPham sanPham = new SanPham();
        mapDtoToEntity(dto, sanPham);
        sanPham.setNgayTao(LocalDateTime.now());

        if (dto.getHinhDaiDien() != null && !dto.getHinhDaiDien().isEmpty()) {
            sanPham.setHinhDaiDien(saveImage(dto.getHinhDaiDien()));
        }

        sanPham = sanPhamRepository.save(sanPham);

        if (dto.getHinhAnhs() != null && !dto.getHinhAnhs().isEmpty()) {
            for (MultipartFile file : dto.getHinhAnhs()) {
                if (file != null && !file.isEmpty()) {
                    HinhAnhSanPham ha = new HinhAnhSanPham();
                    ha.setHinhAnh(saveImage(file));
                    ha.setSanPham(sanPham);
                    hinhAnhSanPhamRepository.save(ha);
                }
            }
        }

        return convertToResponseDTO(sanPham);
    }

    @Transactional
    public SanPhamResponseDTO updateSanPham(Integer id, SanPhamDTO dto) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("San pham khong ton tai voi ID: " + id));
        mapDtoToEntity(dto, sanPham);

        if (dto.getHinhDaiDien() != null && !dto.getHinhDaiDien().isEmpty()) {
            sanPham.setHinhDaiDien(saveImage(dto.getHinhDaiDien()));
        }

        sanPham = sanPhamRepository.save(sanPham);

        if (dto.getHinhAnhs() != null && !dto.getHinhAnhs().isEmpty()) {
            hinhAnhSanPhamRepository.deleteAll(sanPham.getHinhAnhSanPhams());
            for (MultipartFile file : dto.getHinhAnhs()) {
                if (file != null && !file.isEmpty()) {
                    HinhAnhSanPham ha = new HinhAnhSanPham();
                    ha.setHinhAnh(saveImage(file));
                    ha.setSanPham(sanPham);
                    hinhAnhSanPhamRepository.save(ha);
                }
            }
        }

        return convertToResponseDTO(sanPham);
    }

    @Transactional
    public void deleteSanPham(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("San pham khong ton tai voi ID: " + id));
        hinhAnhSanPhamRepository.deleteAll(sanPham.getHinhAnhSanPhams());
        sanPhamRepository.delete(sanPham);
    }

    private void mapDtoToEntity(SanPhamDTO dto, SanPham sanPham) {
        sanPham.setTenSP(dto.getTenSP());
        sanPham.setGiaBan(dto.getGiaBan());
        sanPham.setSlTon(dto.getSlTon());
        sanPham.setKhuyenMai(dto.getKhuyenMai() == null ? BigDecimal.ZERO : dto.getKhuyenMai());
        sanPham.setTinhTrang(dto.getTinhTrang() == null ? 1 : dto.getTinhTrang());
        sanPham.setMoTa(dto.getMoTa());

        LoaiSanPham loaiSanPham = loaiSanPhamRepository.findById(dto.getMaLoai())
                .orElseThrow(() -> new RuntimeException("Loai san pham khong ton tai voi ID: " + dto.getMaLoai()));
        sanPham.setLoaiSanPham(loaiSanPham);

        LoaiThuCung loaiThuCung = loaiThuCungRepository.findById(dto.getMaLoaiTC())
                .orElseThrow(() -> new RuntimeException("Loai thu cung khong ton tai voi ID: " + dto.getMaLoaiTC()));
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
            response.setHinhAnhs(List.of());
        }
        return response;
    }

    private String saveImage(MultipartFile file) {
        try {
            Path uploadDirPath = resolveProductUploadDir();
            Files.createDirectories(uploadDirPath);

            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            String fileName = "product_" + System.currentTimeMillis() + "_"
                    + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");

            Path uploadPath = uploadDirPath.resolve(fileName);
            file.transferTo(uploadPath.toFile());

            return "/uploads/products/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Loi khi luu hinh anh san pham: " + e.getMessage(), e);
        }
    }

    private Path resolveProductUploadDir() {
        Path[] candidates = new Path[]{
                Paths.get("petshop", "src", "main", "resources", "static", "uploads", "products"),
                Paths.get("src", "main", "resources", "static", "uploads", "products")
        };

        for (Path candidate : candidates) {
            Path absoluteCandidate = candidate.toAbsolutePath().normalize();
            Path staticDir = absoluteCandidate.getParent() != null
                    ? absoluteCandidate.getParent().getParent()
                    : null;

            if (staticDir != null && Files.exists(staticDir)) {
                return absoluteCandidate;
            }
        }

        return candidates[0].toAbsolutePath().normalize();
    }
}
