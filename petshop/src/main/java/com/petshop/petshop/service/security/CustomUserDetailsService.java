/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.service.security;

import com.petshop.petshop.model.TaiKhoan;
import com.petshop.petshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // 1. Tìm User trong DB
        TaiKhoan user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Không tìm thấy người dùng với tên đăng nhập: " + username));

        // 2. Chuyển đổi Set<Role> thành Set<GrantedAuthority>
        Set<GrantedAuthority> authorities = user.getLoaiTaiKhoan().stream()
                .map(role -> new SimpleGrantedAuthority(role.getTenLoaiTK()))
                .collect(Collectors.toSet());

        // 3. Trả về đối tượng UserDetails mà Spring Security yêu cầu
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Mật khẩu đã được mã hóa trong DB
                authorities
        );
    }
}