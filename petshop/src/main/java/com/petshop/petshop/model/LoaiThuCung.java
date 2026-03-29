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

import java.util.Set;

@Entity
@Table(name = "LOAI_TC")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoaiThuCung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maLoaiTC;

    private String tenLoaiTC;

    @OneToMany(mappedBy = "loaiThuCung")
    private Set<SanPham> sanPhams;
}