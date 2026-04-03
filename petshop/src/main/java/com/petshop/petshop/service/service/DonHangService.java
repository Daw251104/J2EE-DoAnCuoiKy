package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.DonHangResponse;
import com.petshop.petshop.dto.ThanhToanRequest;
import com.petshop.petshop.model.*;
import com.petshop.petshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonHangService {

    @Autowired
    private DonHangRepository donHangRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    /**
     * Tạo đơn hàng từ giỏ hàng.
     */
    @Transactional
    public DonHang datHang(String username, ThanhToanRequest request) {
        TaiKhoan taiKhoan = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));

        List<GioHang> gioHangItems = gioHangRepository.findByTaiKhoan(taiKhoan);
        if (gioHangItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống, không thể đặt hàng!");
        }

        PhuongThucThanhToan phuongThuc = phuongThucThanhToanRepository.findById(request.getMaLoaiTT())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ!"));

        // Tính tổng tiền
        BigDecimal tongTien = gioHangItems.stream()
                .map(gh -> {
                    BigDecimal gia = gh.getSanPham().getGiaBan();
                    if (gh.getSanPham().getKhuyenMai() != null
                            && gh.getSanPham().getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal kmPerc = gh.getSanPham().getKhuyenMai().divide(BigDecimal.valueOf(100));
                        gia = gia.multiply(BigDecimal.ONE.subtract(kmPerc));
                    }
                    return gia.multiply(BigDecimal.valueOf(gh.getSoLuong()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo đơn hàng
        DonHang donHang = new DonHang();
        donHang.setNgayLapDonHang(LocalDateTime.now());
        donHang.setTenNguoiNhan(request.getTenNguoiNhan());
        donHang.setSdtNguoiNhan(request.getSdtNguoiNhan());
        donHang.setDiaChiNhanHang(request.getDiaChiNhanHang());
        donHang.setTongTien(tongTien);
        donHang.setTrangThai("CHO_XAC_NHAN");
        donHang.setDaThanhToan(0);
        donHang.setKhachHang(taiKhoan);
        donHang.setPhuongThucThanhToan(phuongThuc);

        DonHang savedDonHang = donHangRepository.save(donHang);

        // Tạo chi tiết đơn hàng
        for (GioHang gh : gioHangItems) {
            ChiTietDonHang chiTiet = new ChiTietDonHang();
            chiTiet.setDonHang(savedDonHang);
            chiTiet.setSanPham(gh.getSanPham());
            chiTiet.setSl(gh.getSoLuong());
            BigDecimal giaBanRa = gh.getSanPham().getGiaBan();
            if (gh.getSanPham().getKhuyenMai() != null
                    && gh.getSanPham().getKhuyenMai().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal kmPerc = gh.getSanPham().getKhuyenMai().divide(BigDecimal.valueOf(100));
                giaBanRa = giaBanRa.multiply(BigDecimal.ONE.subtract(kmPerc));
            }
            chiTiet.setGiaBanRa(giaBanRa);
            chiTietDonHangRepository.save(chiTiet);
        }

        // Xóa giỏ hàng sau khi đặt hàng
        gioHangRepository.deleteAll(gioHangItems);

        return savedDonHang;
    }

    /**
     * Lấy lịch sử đơn hàng của user.
     */
    public List<DonHangResponse> layDanhSachDonHang(String username, String trangThai) {
        TaiKhoan taiKhoan = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));
        List<DonHang> danhSach = donHangRepository.findByKhachHangOrderByNgayLapDonHangDesc(taiKhoan);
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            danhSach = danhSach.stream()
                    .filter(d -> trangThai.equals(d.getTrangThai()))
                    .collect(Collectors.toList());
        }
        return danhSach.stream()
                .map(DonHangResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một đơn hàng.
     */
    public DonHangResponse layChiTietDonHang(Integer maDH, String username) {
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDH));

        if (!donHang.getKhachHang().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này!");
        }

        return DonHangResponse.from(donHang);
    }

    // ===================== STAFF / OWNER OPERATIONS =====================

    /**
     * Lấy tất cả đơn hàng (OWNER/STAFF).
     */
    public List<DonHangResponse> layTatCaDonHang(String trangThai) {
        List<DonHang> danhSach = donHangRepository.findAll();
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            danhSach = danhSach.stream()
                    .filter(d -> trangThai.equals(d.getTrangThai()))
                    .collect(Collectors.toList());
        }
        return danhSach.stream()
                .sorted((a, b) -> b.getNgayLapDonHang().compareTo(a.getNgayLapDonHang()))
                .map(DonHangResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết đơn hàng cho staff (không kiểm tra quyền sở hữu).
     */
    public DonHangResponse layChiTietDonHangStaff(Integer maDH) {
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDH));
        return DonHangResponse.from(donHang);
    }

    /**
     * Xác nhận đơn hàng → chuyển sang DANG_GIAO (OWNER/STAFF).
     */
    @Transactional
    public void xacNhanDonHang(Integer maDH, String staffUsername) {
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDH));

        if (!"CHO_XAC_NHAN".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận!");
        }

        TaiKhoan staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản staff"));

        donHang.setTrangThai("DANG_GIAO");
        donHang.setNguoiXuLy(staff);
        donHangRepository.save(donHang);
    }

    /**
     * Từ chối đơn hàng → chuyển sang DA_HUY (OWNER/STAFF).
     */
    @Transactional
    public void tuChoiDonHang(Integer maDH, String staffUsername) {
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDH));

        if (!"CHO_XAC_NHAN".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận!");
        }

        TaiKhoan staff = userRepository.findByUsername(staffUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản staff"));

        donHang.setTrangThai("DA_HUY");
        donHang.setNguoiXuLy(staff);
        donHangRepository.save(donHang);
    }

    // ===================== CUSTOMER OPERATIONS =====================

    /**
     * Khách hàng xác nhận đã nhận hàng → DA_GIAO.
     */
    @Transactional
    public void xacNhanDaGiao(Integer maDH, String username) {
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + maDH));

        if (!donHang.getKhachHang().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này!");
        }

        if (!"DANG_GIAO".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng chưa ở trạng thái đang giao!");
        }

        donHang.setTrangThai("DA_GIAO");
        
        // Giảm số lượng tồn kho của từng sản phẩm trong đơn hàng
        if (donHang.getChiTietDonHangs() != null) {
            for (ChiTietDonHang ct : donHang.getChiTietDonHangs()) {
                SanPham sp = ct.getSanPham();
                int slConLai = (sp.getSlTon() != null ? sp.getSlTon() : 0) - ct.getSl();
                if (slConLai < 0) {
                    slConLai = 0; // Tránh số lượng tồn âm
                }
                sp.setSlTon(slConLai);
                sanPhamRepository.save(sp);
            }
        }
        
        donHangRepository.save(donHang);
    }
}
