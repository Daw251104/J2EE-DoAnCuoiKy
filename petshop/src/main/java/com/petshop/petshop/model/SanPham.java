/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.model;

/**
 *
 * @author datp4
 */
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "SAN_PHAM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maSP;

    private String tenSP;

    private BigDecimal giaBan;

    private Integer slTon;

    private BigDecimal khuyenMai;

    private Integer tinhTrang;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    private String hinhDaiDien;

    private LocalDateTime ngayTao;

    @ManyToOne
    @JoinColumn(name = "MALOAI")
    private LoaiSanPham loaiSanPham;

    @OneToMany(mappedBy = "sanPham")
    private Set<HinhAnhSanPham> hinhAnhSanPhams;

    @ManyToOne
    @JoinColumn(name = "MALOAITC")
    private LoaiThuCung loaiThuCung;
}
