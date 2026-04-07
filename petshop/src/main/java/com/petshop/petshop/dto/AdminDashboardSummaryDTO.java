package com.petshop.petshop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminDashboardSummaryDTO {
    private long tongDonHang;
    private long donChoXacNhan;
    private long donDangGiao;
    private long donDaGiao;
    private long donDaHuy;
    private long tongSanPhamDaBan;

    private BigDecimal doanhThu = BigDecimal.ZERO;
    private BigDecimal chiPhiUocTinh = BigDecimal.ZERO;
    private BigDecimal loiNhuanUocTinh = BigDecimal.ZERO;

    private BigDecimal doanhThuThangNay = BigDecimal.ZERO;
    private BigDecimal doanhThuThangTruoc = BigDecimal.ZERO;
    private BigDecimal bienDongDoanhThuPhanTram = BigDecimal.ZERO;

    private BigDecimal tyLeGiaVon = BigDecimal.ZERO;
    private BigDecimal tyLeGiaVonPhanTram = BigDecimal.ZERO;

    private List<AdminTopProductDTO> topSanPhams = new ArrayList<>();
    private List<AdminRecentOrderDTO> donGanDays = new ArrayList<>();
}
