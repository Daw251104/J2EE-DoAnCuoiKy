package com.petshop.petshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GioHangResponse {
    private Integer maGH;
    private Integer maSP;
    private String tenSP;
    private BigDecimal giaBan;
    private BigDecimal khuyenMai;
    private String hinhDaiDien;
    private Integer soLuong;
    private BigDecimal thanhTien;

    public static GioHangResponse from(com.petshop.petshop.model.GioHang gh) {
        GioHangResponse res = new GioHangResponse();
        res.setMaGH(gh.getMaGH());
        res.setMaSP(gh.getSanPham().getMaSP());
        res.setTenSP(gh.getSanPham().getTenSP());
        res.setGiaBan(gh.getSanPham().getGiaBan());
        res.setKhuyenMai(gh.getSanPham().getKhuyenMai());
        res.setHinhDaiDien(gh.getSanPham().getHinhDaiDien());
        res.setSoLuong(gh.getSoLuong());

        // Tính thành tiền = giaBan * (1 - khuyenMai/100) * soLuong
        BigDecimal gia = gh.getSanPham().getGiaBan();
        BigDecimal km = gh.getSanPham().getKhuyenMai();
        BigDecimal giaSauKM = gia;
        if (km != null && km.compareTo(BigDecimal.ZERO) > 0) {
            giaSauKM = gia.multiply(BigDecimal.ONE.subtract(km.divide(BigDecimal.valueOf(100))));
        }
        res.setThanhTien(giaSauKM.multiply(BigDecimal.valueOf(gh.getSoLuong())));
        return res;
    }
}
