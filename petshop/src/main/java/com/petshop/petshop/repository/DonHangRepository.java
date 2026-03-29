package com.petshop.petshop.repository;

import com.petshop.petshop.model.DonHang;
import com.petshop.petshop.model.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

    List<DonHang> findByKhachHangOrderByNgayLapDonHangDesc(TaiKhoan khachHang);
}
