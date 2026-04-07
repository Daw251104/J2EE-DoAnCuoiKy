package com.petshop.petshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRecentOrderDTO {
    private Integer maDH;
    private String tenNguoiNhan;
    private BigDecimal tongTien;
    private String trangThai;
    private LocalDateTime ngayLapDonHang;
}

