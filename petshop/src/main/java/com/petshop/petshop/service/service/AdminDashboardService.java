package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.AdminDashboardSummaryDTO;
import com.petshop.petshop.dto.AdminRecentOrderDTO;
import com.petshop.petshop.dto.AdminTopProductDTO;
import com.petshop.petshop.model.DonHang;
import com.petshop.petshop.repository.ChiTietDonHangRepository;
import com.petshop.petshop.repository.DonHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final String TRANG_THAI_DA_GIAO = "DA_GIAO";
    private static final String TRANG_THAI_DANG_GIAO = "DANG_GIAO";
    private static final String TRANG_THAI_CHO_XAC_NHAN = "CHO_XAC_NHAN";
    private static final String TRANG_THAI_DA_HUY = "DA_HUY";

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    @Value("${admin.dashboard.cost-ratio:0.60}")
    private BigDecimal tyLeGiaVon;

    public AdminDashboardSummaryDTO layTongQuan() {
        AdminDashboardSummaryDTO summary = new AdminDashboardSummaryDTO();

        long tongDonHang = donHangRepository.count();
        long donChoXacNhan = donHangRepository.countByTrangThai(TRANG_THAI_CHO_XAC_NHAN);
        long donDangGiao = donHangRepository.countByTrangThai(TRANG_THAI_DANG_GIAO);
        long donDaGiao = donHangRepository.countByTrangThai(TRANG_THAI_DA_GIAO);
        long donDaHuy = donHangRepository.countByTrangThai(TRANG_THAI_DA_HUY);

        BigDecimal doanhThu = zeroIfNull(donHangRepository.tongDoanhThuByTrangThai(TRANG_THAI_DA_GIAO));
        BigDecimal chiPhiUocTinh = doanhThu.multiply(safeCostRatio());
        BigDecimal loiNhuanUocTinh = doanhThu.subtract(chiPhiUocTinh);

        Long tongSoLuongDaBan = chiTietDonHangRepository.tongSoLuongTheoTrangThai(TRANG_THAI_DA_GIAO);

        List<AdminTopProductDTO> topSanPhams = chiTietDonHangRepository.thongKeTopSanPhamTheoTrangThai(
                TRANG_THAI_DA_GIAO, PageRequest.of(0, 6)
        );
        if (topSanPhams == null) {
            topSanPhams = Collections.emptyList();
        }

        List<AdminRecentOrderDTO> donGanDays = donHangRepository.findTop8ByOrderByNgayLapDonHangDesc().stream()
                .map(this::toRecentOrderDto)
                .toList();

        LocalDateTime dauThangNay = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime dauThangSau = dauThangNay.plusMonths(1);
        LocalDateTime dauThangTruoc = dauThangNay.minusMonths(1);

        BigDecimal doanhThuThangNay = zeroIfNull(
                donHangRepository.tongDoanhThuByTrangThaiVaKhoangNgay(TRANG_THAI_DA_GIAO, dauThangNay, dauThangSau)
        );
        BigDecimal doanhThuThangTruoc = zeroIfNull(
                donHangRepository.tongDoanhThuByTrangThaiVaKhoangNgay(TRANG_THAI_DA_GIAO, dauThangTruoc, dauThangNay)
        );

        summary.setTongDonHang(tongDonHang);
        summary.setDonChoXacNhan(donChoXacNhan);
        summary.setDonDangGiao(donDangGiao);
        summary.setDonDaGiao(donDaGiao);
        summary.setDonDaHuy(donDaHuy);
        summary.setTongSanPhamDaBan(tongSoLuongDaBan == null ? 0L : tongSoLuongDaBan);
        summary.setDoanhThu(doanhThu);
        summary.setChiPhiUocTinh(chiPhiUocTinh);
        summary.setLoiNhuanUocTinh(loiNhuanUocTinh);
        summary.setDoanhThuThangNay(doanhThuThangNay);
        summary.setDoanhThuThangTruoc(doanhThuThangTruoc);
        summary.setBienDongDoanhThuPhanTram(tinhTyLeBienDong(doanhThuThangNay, doanhThuThangTruoc));
        summary.setTyLeGiaVon(safeCostRatio());
        summary.setTyLeGiaVonPhanTram(safeCostRatio().multiply(BigDecimal.valueOf(100)));
        summary.setTopSanPhams(topSanPhams);
        summary.setDonGanDays(donGanDays);

        return summary;
    }

    private AdminRecentOrderDTO toRecentOrderDto(DonHang donHang) {
        return new AdminRecentOrderDTO(
                donHang.getMaDH(),
                donHang.getTenNguoiNhan(),
                zeroIfNull(donHang.getTongTien()),
                donHang.getTrangThai(),
                donHang.getNgayLapDonHang()
        );
    }

    private BigDecimal safeCostRatio() {
        if (tyLeGiaVon == null) {
            return new BigDecimal("0.60");
        }
        if (tyLeGiaVon.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (tyLeGiaVon.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ONE;
        }
        return tyLeGiaVon;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal tinhTyLeBienDong(BigDecimal hienTai, BigDecimal thangTruoc) {
        BigDecimal current = zeroIfNull(hienTai);
        BigDecimal previous = zeroIfNull(thangTruoc);

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0
                    ? new BigDecimal("100.00")
                    : BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }
}
