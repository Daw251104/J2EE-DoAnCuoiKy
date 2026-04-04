/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.repository;

import com.petshop.petshop.model.LoaiThuCung;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoaiThuCungRepository extends JpaRepository<LoaiThuCung, Integer> {
    Optional<LoaiThuCung> findByTenLoaiTC(String tenLoaiTC);
}