/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.repository;

import com.petshop.petshop.model.SanPham;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author datp4
 */
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    Optional<SanPham> findByTenSP(String tenSP);

    @Query("SELECT s FROM SanPham s WHERE (:maLoaiTC IS NULL OR s.loaiThuCung.maLoaiTC = :maLoaiTC) AND (:maLoai IS NULL OR s.loaiSanPham.maLoai = :maLoai)")
    Page<SanPham> locSanPham(@Param("maLoaiTC") Integer maLoaiTC, @Param("maLoai") Integer maLoai, Pageable pageable);
}
