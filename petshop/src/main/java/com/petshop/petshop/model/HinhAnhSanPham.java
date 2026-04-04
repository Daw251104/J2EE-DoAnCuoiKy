/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "HINH_ANH_SAN_PHAM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HinhAnhSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maHinhAnh;

    private String hinhAnh;

    @ManyToOne
    @JoinColumn(name = "MASP")
    private SanPham sanPham;
}
