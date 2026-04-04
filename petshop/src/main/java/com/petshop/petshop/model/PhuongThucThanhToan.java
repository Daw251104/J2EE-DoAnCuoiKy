/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "PHUONG_THUC_THANH_TOAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhuongThucThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maLoaiTT;

    private String tenLoaiTT;

    @OneToMany(mappedBy = "phuongThucThanhToan")
    private Set<DonHang> donHangs;
}
