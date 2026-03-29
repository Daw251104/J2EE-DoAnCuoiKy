/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SanPhamResponseDTO {
    private Integer maSP;
    private String tenSP;
    private BigDecimal giaBan;
    private Integer slTon;
    private BigDecimal khuyenMai;
    private Integer tinhTrang;
    private String moTa;
    private String hinhDaiDien;
    private LocalDateTime ngayTao;
    private String tenLoaiSanPham;
    private String tenLoaiThuCung;
    private List<String> hinhAnhs;
}