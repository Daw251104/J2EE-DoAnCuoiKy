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

@Entity
@Table(name = "CT_DH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ChiTietDonHangId.class)
public class ChiTietDonHang {

    @Id
    @ManyToOne
    @JoinColumn(name = "MADH")
    private DonHang donHang;

    @Id
    @ManyToOne
    @JoinColumn(name = "MASP")
    private SanPham sanPham;

    private Integer sl;

    private BigDecimal giaBanRa;
}