/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.dto;

import com.petshop.petshop.model.GioiTinh;
import com.petshop.petshop.model.TrangThaiTaiKhoan;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaiKhoanResponse {

    private Integer maTK;
    private String username;
    private String hoTen;
    private GioiTinh gioiTinh;
    private String sdt;
    private String email;
    private String diaChi;
    private String anhDaiDien;
    private TrangThaiTaiKhoan trangThai;
    private LocalDateTime ngayTao;
}