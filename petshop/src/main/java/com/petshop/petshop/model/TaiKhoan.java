/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "TAI_KHOAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maTK;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String hoTen;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private GioiTinh gioiTinh;

    @Column(length = 15)
    private String sdt;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 255)
    private String diaChi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrangThaiTaiKhoan trangThai = TrangThaiTaiKhoan.PENDING;

    @Column(length = 255)
    private String anhDaiDien;  // đường dẫn ảnh đại diện

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "TAI_KHOAN_LOAI_TK",
        joinColumns = @JoinColumn(name = "maTK"),
        inverseJoinColumns = @JoinColumn(name = "maLoaiTK")
    )
    private Set<LoaiTaiKhoan> loaiTaiKhoan;

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DonHang> donHangs;
}