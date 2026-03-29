package com.petshop.petshop.repository;

import com.petshop.petshop.model.GioHang;
import com.petshop.petshop.model.SanPham;
import com.petshop.petshop.model.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    List<GioHang> findByTaiKhoan(TaiKhoan taiKhoan);

    Optional<GioHang> findByTaiKhoanAndSanPham(TaiKhoan taiKhoan, SanPham sanPham);
}
