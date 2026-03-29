/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.repository;

import com.petshop.petshop.model.LoaiTaiKhoan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author datp4
 */
public interface RoleRepository extends JpaRepository <LoaiTaiKhoan , Integer> {
    Optional<LoaiTaiKhoan> findByTenLoaiTK(String tenLoaiTK);
}
