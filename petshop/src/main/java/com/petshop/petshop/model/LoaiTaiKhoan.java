/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "LOAI_TK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoaiTaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maLoaiTK;

    private String tenLoaiTK;

    @ManyToMany(mappedBy = "loaiTaiKhoan")
    private Set<TaiKhoan> taiKhoans;
}