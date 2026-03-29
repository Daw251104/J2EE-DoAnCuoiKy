/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petshop.petshop.service.security;

import com.petshop.petshop.dto.RegisterRequest;
import com.petshop.petshop.dto.TaiKhoanResponse;
import com.petshop.petshop.model.LoaiTaiKhoan;
import com.petshop.petshop.model.TaiKhoan;
import com.petshop.petshop.model.TrangThaiTaiKhoan;
import com.petshop.petshop.repository.RoleRepository;
import com.petshop.petshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository taiKhoanRepository;
    private final RoleRepository loaiTaiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TaiKhoanResponse register(RegisterRequest request) {
        if (taiKhoanRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        String anhDaiDienPath = null;
        MultipartFile file = request.getAnhDaiDien();
        if (file != null && !file.isEmpty()) {
            try {
                // Cách 1: Dùng ClassPathResource (khuyến nghị cho static resources)
                // Nhưng ClassPathResource chỉ đọc được, không ghi → cần dùng FileSystem
                // Cách tốt hơn: kết hợp project root + relative path
                String projectRoot = System.getProperty("user.dir");  // thư mục gốc dự án (chứa pom.xml)
                String relativePath = "/src/main/resources/static/uploads/avatars/";
                String uploadDir = projectRoot + relativePath;

                // Normalize đường dẫn để tránh lỗi
                Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(uploadDirPath);  // tạo thư mục nếu chưa có

                String fileName = "avatar_" + System.currentTimeMillis() + "_" 
                                  + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");  // sanitize tên file

                Path uploadPath = uploadDirPath.resolve(fileName);

                // Lưu file
                file.transferTo(uploadPath.toFile());

                // Đường dẫn relative để lưu vào DB và phục vụ static
                anhDaiDienPath = "/uploads/avatars/" + fileName;

                System.out.println("Ảnh đã lưu tại: " + uploadPath.toString());  // log để debug

            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh đại diện: " + e.getMessage(), e);
            }
        }

        TaiKhoan taiKhoan = TaiKhoan.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .hoTen(request.getHoTen())
                .gioiTinh(request.getGioiTinh())
                .sdt(request.getSdt())
                .email(request.getEmail())
                .diaChi(request.getDiaChi())
                .anhDaiDien(anhDaiDienPath)
                .trangThai(TrangThaiTaiKhoan.ACTIVE)  // hoặc PENDING nếu cần xác thực email
                .loaiTaiKhoan(new HashSet<>())
                .build();

        // Gán role mặc định (giả sử có role "CUSTOMER")
        LoaiTaiKhoan defaultRole = loaiTaiKhoanRepository.findByTenLoaiTK("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role mặc định"));
        taiKhoan.getLoaiTaiKhoan().add(defaultRole);

        TaiKhoan saved = taiKhoanRepository.save(taiKhoan);

        return TaiKhoanResponse.builder()
                .maTK(saved.getMaTK())
                .username(saved.getUsername())
                .hoTen(saved.getHoTen())
                .gioiTinh(saved.getGioiTinh())
                .sdt(saved.getSdt())
                .email(saved.getEmail())
                .diaChi(saved.getDiaChi())
                .anhDaiDien(saved.getAnhDaiDien())
                .trangThai(saved.getTrangThai())
                .ngayTao(saved.getNgayTao())
                .build();
    }
}