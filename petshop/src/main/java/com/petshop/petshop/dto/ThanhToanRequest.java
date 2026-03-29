package com.petshop.petshop.dto;

import lombok.Data;

@Data
public class ThanhToanRequest {

    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiNhanHang;
    private Integer maLoaiTT;
}
