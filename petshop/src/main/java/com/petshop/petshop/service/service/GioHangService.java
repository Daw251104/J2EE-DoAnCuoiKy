package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.GioHangRequest;
import com.petshop.petshop.dto.GioHangResponse;
import com.petshop.petshop.model.GioHang;
import com.petshop.petshop.model.SanPham;
import com.petshop.petshop.model.TaiKhoan;
import com.petshop.petshop.repository.GioHangRepository;
import com.petshop.petshop.repository.SanPhamRepository;
import com.petshop.petshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GioHangService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    /**
     * Thêm sản phẩm vào giỏ hàng.
     * Nếu sản phẩm đã tồn tại trong giỏ thì cộng dồn số lượng.
     */
    @Transactional
    public void themVaoGioHang(String username, GioHangRequest request) {
        TaiKhoan taiKhoan = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));

        SanPham sanPham = sanPhamRepository.findById(request.getMaSP())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + request.getMaSP()));

        int soLuongThem = (request.getSoLuong() != null && request.getSoLuong() > 0)
                ? request.getSoLuong() : 1;

        Optional<GioHang> existing = gioHangRepository.findByTaiKhoanAndSanPham(taiKhoan, sanPham);
        if (existing.isPresent()) {
            // Cộng dồn số lượng
            GioHang gh = existing.get();
            gh.setSoLuong(gh.getSoLuong() + soLuongThem);
            gioHangRepository.save(gh);
        } else {
            // Thêm mới
            GioHang gh = new GioHang();
            gh.setTaiKhoan(taiKhoan);
            gh.setSanPham(sanPham);
            gh.setSoLuong(soLuongThem);
            gioHangRepository.save(gh);
        }
    }

    /**
     * Lấy danh sách giỏ hàng của user.
     */
    public List<GioHangResponse> layGioHang(String username) {
        TaiKhoan taiKhoan = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));
        return gioHangRepository.findByTaiKhoan(taiKhoan)
                .stream()
                .map(GioHangResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ.
     */
    @Transactional
    public void capNhatSoLuong(Integer maGH, Integer soLuong, String username) {
        GioHang gh = gioHangRepository.findById(maGH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng: " + maGH));

        // Kiểm tra giỏ hàng thuộc về user đang đăng nhập
        if (!gh.getTaiKhoan().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền sửa giỏ hàng này!");
        }

        if (soLuong == null || soLuong <= 0) {
            gioHangRepository.delete(gh);
        } else {
            gh.setSoLuong(soLuong);
            gioHangRepository.save(gh);
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng.
     */
    @Transactional
    public void xoaKhoiGioHang(Integer maGH, String username) {
        GioHang gh = gioHangRepository.findById(maGH)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng: " + maGH));

        if (!gh.getTaiKhoan().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xóa giỏ hàng này!");
        }

        gioHangRepository.delete(gh);
    }

    /**
     * Tính tổng tiền giỏ hàng.
     */
    public BigDecimal tinhTongTien(List<GioHangResponse> items) {
        return items.stream()
                .map(GioHangResponse::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
