package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.AdminCreateAccountRequest;
import com.petshop.petshop.model.*;
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
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /** Danh sách tất cả tài khoản */
    public List<TaiKhoan> getAllAccounts() {
        return userRepository.findAll();
    }

    /** Danh sách tất cả loại tài khoản */
    public List<LoaiTaiKhoan> getAllRoles() {
        return roleRepository.findAll();
    }

    /** Tạo tài khoản (admin chọn LoaiTaiKhoan) */
    @Transactional
    public TaiKhoan createAccount(AdminCreateAccountRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        String anhDaiDienPath = null;
        MultipartFile file = req.getAnhDaiDien();
        if (file != null && !file.isEmpty()) {
            anhDaiDienPath = saveAvatar(file);
        }

        Set<LoaiTaiKhoan> selectedRoles = new HashSet<>();
        if (req.getLoaiTaiKhoans() != null && !req.getLoaiTaiKhoans().isEmpty()) {
            for (Integer maLoaiTK : req.getLoaiTaiKhoans()) {
                LoaiTaiKhoan role = roleRepository.findById(maLoaiTK)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy role ID: " + maLoaiTK));
                selectedRoles.add(role);
            }
        } else {
            // Mặc định CUSTOMER nếu không chọn
            LoaiTaiKhoan defaultRole = roleRepository.findByTenLoaiTK("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role mặc định CUSTOMER"));
            selectedRoles.add(defaultRole);
        }

        TaiKhoan taiKhoan = TaiKhoan.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .hoTen(req.getHoTen())
                .gioiTinh(req.getGioiTinh())
                .sdt(req.getSdt())
                .email(req.getEmail())
                .diaChi(req.getDiaChi())
                .anhDaiDien(anhDaiDienPath)
                .trangThai(TrangThaiTaiKhoan.ACTIVE)
                .loaiTaiKhoan(selectedRoles)
                .build();

        return userRepository.save(taiKhoan);
    }

    /** Đổi trạng thái tài khoản */
    @Transactional
    public TaiKhoan updateStatus(Integer maTK, TrangThaiTaiKhoan trangThai) {
        TaiKhoan taiKhoan = userRepository.findById(maTK)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ID: " + maTK));
        taiKhoan.setTrangThai(trangThai);
        return userRepository.save(taiKhoan);
    }

    private String saveAvatar(MultipartFile file) {
        try {
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + "/uploads/avatars/";
            Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadDirPath);

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String fileName = "avatar_" + System.currentTimeMillis() + "_"
                    + originalName.replaceAll("[^a-zA-Z0-9.-]", "_");

            Path uploadPath = uploadDirPath.resolve(fileName);
            file.transferTo(uploadPath.toFile());

            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh đại diện: " + e.getMessage(), e);
        }
    }
}
