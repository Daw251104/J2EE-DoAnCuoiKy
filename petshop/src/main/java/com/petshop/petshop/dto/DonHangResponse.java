package com.petshop.petshop.dto;

import com.petshop.petshop.model.ChiTietDonHang;
import com.petshop.petshop.model.DonHang;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DonHangResponse {

    private Integer maDH;
    private LocalDateTime ngayLapDonHang;
    private BigDecimal tongTien;
    private String trangThai;
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiNhanHang;
    private String tenLoaiTT;
    private Integer daThanhToan;
    private List<ChiTietDTO> chiTietDonHangs;

    @Data
    public static class ChiTietDTO {
        private String tenSP;
        private String hinhDaiDien;
        private Integer soLuong;
        private BigDecimal giaBanRa;
        private BigDecimal thanhTien;
    }

    public static DonHangResponse from(DonHang dh) {
        DonHangResponse res = new DonHangResponse();
        res.setMaDH(dh.getMaDH());
        res.setNgayLapDonHang(dh.getNgayLapDonHang());
        res.setTongTien(dh.getTongTien());
        res.setTrangThai(dh.getTrangThai());
        res.setTenNguoiNhan(dh.getTenNguoiNhan());
        res.setSdtNguoiNhan(dh.getSdtNguoiNhan());
        res.setDiaChiNhanHang(dh.getDiaChiNhanHang());
        res.setDaThanhToan(dh.getDaThanhToan());
        if (dh.getPhuongThucThanhToan() != null) {
            res.setTenLoaiTT(dh.getPhuongThucThanhToan().getTenLoaiTT());
        }
        if (dh.getChiTietDonHangs() != null) {
            res.setChiTietDonHangs(
                dh.getChiTietDonHangs().stream().map(ct -> {
                    ChiTietDTO dto = new ChiTietDTO();
                    dto.setTenSP(ct.getSanPham().getTenSP());
                    dto.setHinhDaiDien(ct.getSanPham().getHinhDaiDien());
                    dto.setSoLuong(ct.getSl());
                    dto.setGiaBanRa(ct.getGiaBanRa());
                    dto.setThanhTien(ct.getGiaBanRa().multiply(BigDecimal.valueOf(ct.getSl())));
                    return dto;
                }).collect(Collectors.toList())
            );
        }
        return res;
    }
}
