package com.petshop.petshop.repository;

import com.petshop.petshop.model.ChiTietDonHang;
import com.petshop.petshop.model.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {

    List<ChiTietDonHang> findByDonHang(DonHang donHang);
}
