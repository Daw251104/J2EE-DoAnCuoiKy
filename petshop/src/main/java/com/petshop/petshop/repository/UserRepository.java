/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.repository;

import com.petshop.petshop.model.TaiKhoan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByUsername(String username);

    Optional<TaiKhoan> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
