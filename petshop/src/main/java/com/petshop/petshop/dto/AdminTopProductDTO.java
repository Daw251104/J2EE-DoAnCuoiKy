package com.petshop.petshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminTopProductDTO {
    private Integer maSP;
    private String tenSP;
    private String hinhDaiDien;
    private Long soLuongBan;
    private BigDecimal doanhThu;
}

