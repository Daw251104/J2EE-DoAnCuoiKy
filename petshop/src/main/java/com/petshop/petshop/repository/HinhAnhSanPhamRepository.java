/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.repository;

import com.petshop.petshop.model.HinhAnhSanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HinhAnhSanPhamRepository extends JpaRepository<HinhAnhSanPham, Integer> {
    List<HinhAnhSanPham> findAllByMaHinhAnhInAndSanPham_MaSP(List<Integer> maHinhAnhIds, Integer maSP);
    Optional<HinhAnhSanPham> findByMaHinhAnhAndSanPham_MaSP(Integer maHinhAnh, Integer maSP);
    void deleteByMaHinhAnhInAndSanPham_MaSP(List<Integer> maHinhAnhIds, Integer maSP);
}
