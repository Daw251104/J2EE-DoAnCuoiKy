/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GIO_HANG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maGH;

    private Integer soLuong;

    @ManyToOne
    @JoinColumn(name = "MAKH")
    private TaiKhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "MASP")
    private SanPham sanPham;
}
