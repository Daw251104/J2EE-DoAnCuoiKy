/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "DON_HANG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDH;

    private LocalDateTime ngayLapDonHang;

    private String tenNguoiNhan;

    private String sdtNguoiNhan;

    private String diaChiNhanHang;

    private BigDecimal tongTien;

    private String trangThai;

    private Integer daThanhToan;

    private String maVanChuyen;

    @ManyToOne
    @JoinColumn(name = "MAKH")
    private TaiKhoan khachHang;

    @ManyToOne
    @JoinColumn(name = "MALOAITT")
    private PhuongThucThanhToan phuongThucThanhToan;

    @ManyToOne
    @JoinColumn(name = "NGUOI_XULY")
    private TaiKhoan nguoiXuLy;

    @OneToMany(mappedBy = "donHang")
    private Set<ChiTietDonHang> chiTietDonHangs;
}