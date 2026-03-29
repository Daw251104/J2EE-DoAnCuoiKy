/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SanPhamDTO {
    private Integer maSP;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String tenSP;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán phải >= 0")
    private BigDecimal giaBan;

    @NotNull(message = "Số lượng tồn không được để trống")
    @Min(value = 0, message = "Số lượng tồn phải >= 0")
    private Integer slTon;

    private BigDecimal khuyenMai = BigDecimal.ZERO;

    private Integer tinhTrang = 1; // Mặc định hoạt động

    private String moTa;

    private MultipartFile hinhDaiDien;

    @NotNull(message = "Mã loại sản phẩm không được để trống")
    private Integer maLoai; // ID của LoaiSanPham

    @NotNull(message = "Mã loại thú cưng không được để trống")
    private Integer maLoaiTC; // ID của LoaiThuCung

    private List<MultipartFile> hinhAnhs; // List file hình ảnh (nếu có)
}
