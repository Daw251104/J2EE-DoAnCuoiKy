/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.config;


import com.petshop.petshop.service.security.CustomUserDetailsService;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Kích hoạt bảo mật web
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Bean mã hóa mật khẩu (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean cấu hình chuỗi bộ lọc bảo mật
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/**")   // ← Thêm dòng này
        )
        // Cấu hình phân quyền truy cập (Authorization)
        .authorizeHttpRequests(authorize -> authorize

            // Các trang yêu cầu vai trò ADMIN hoặc QUAN_TRI_VIEN
            .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "QUAN_TRI_VIEN")

            // Các trang yêu cầu vai trò OWNER hoặc CHU_CUA_HANG
            .requestMatchers("/owner/**", "/sanpham/them", "/sanpham/sua/**", "/sanpham/xoa/**", 
                             "/sanpham/loai/them", "/sanpham/loaitc/them").hasAnyAuthority("OWNER", "STAFF")

            // Quản lý đơn hàng – OWNER / STAFF
            .requestMatchers("/staff/**").hasAnyAuthority("OWNER", "STAFF")

            // Các trang yêu cầu vai trò CUSTOMER hoặc KHACH_HANG
            .requestMatchers("/customer/**").hasAnyAuthority("CUSTOMER", "KHACH_HANG")

            // Giỏ hàng – chỉ dành cho CUSTOMER / KHACH_HANG
            .requestMatchers("/gio-hang/**").hasAnyAuthority("CUSTOMER", "KHACH_HANG")

            // Thanh toán & đơn hàng – chỉ dành cho CUSTOMER / KHACH_HANG
            .requestMatchers("/thanh-toan", "/thanh-toan/**", "/don-hang", "/don-hang/**")
                .hasAnyAuthority("CUSTOMER", "KHACH_HANG")

            // Các trang công khai (permitAll)
            .requestMatchers("/", "/home", "/login", "/register", "/css/**", "/js/**", "/images/**", "/error", "/sanpham", "/uploads/**", "/api/chatbot/**").permitAll()

            // Bất kỳ yêu cầu nào khác đều cần xác thực
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)  // thay bằng trang chính của bạn
                .failureUrl("/login?error")
                .permitAll()
        )        
        .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")  // thay bằng trang chính của bạn
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        )
        .userDetailsService(customUserDetailsService);
        return http.build();
        // Cấu hình Form Đăng nhập
    }
    @Bean
    public LayoutDialect layoutDialect(){
        return new LayoutDialect();
    }
}
