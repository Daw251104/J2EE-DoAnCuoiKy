package com.petshop.petshop.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SanPhamDTO {
    private Integer maSP;

    @NotBlank(message = "Ten san pham khong duoc de trong")
    private String tenSP;

    @NotNull(message = "Gia ban khong duoc de trong")
    @Min(value = 0, message = "Gia ban phai >= 0")
    @Digits(integer = 12, fraction = 0, message = "Gia ban phai la so nguyen (VND)")
    private BigDecimal giaBan;

    @NotNull(message = "So luong ton khong duoc de trong")
    @Min(value = 0, message = "So luong ton phai >= 0")
    private Integer slTon;

    private BigDecimal khuyenMai = BigDecimal.ZERO;
    private Integer tinhTrang = 1;
    private String moTa;
    private MultipartFile hinhDaiDien;

    @NotNull(message = "Ma loai san pham khong duoc de trong")
    private Integer maLoai;

    @NotNull(message = "Ma loai thu cung khong duoc de trong")
    private Integer maLoaiTC;

    private List<MultipartFile> hinhAnhs;
    private List<Integer> xoaHinhAnhIds;
}