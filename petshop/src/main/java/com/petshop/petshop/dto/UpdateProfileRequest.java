package com.petshop.petshop.dto;

import com.petshop.petshop.model.GioiTinh;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String hoTen;
    private String sdt;
    private GioiTinh gioiTinh;
    private String diaChi;
    private String email;
    private String anhDaiDien;
}
