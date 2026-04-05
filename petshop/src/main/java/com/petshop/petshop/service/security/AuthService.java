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
            throw new RuntimeException("Ten dang nhap da ton tai");
        }
        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email da ton tai");
        }

        String anhDaiDienPath = null;
        MultipartFile file = request.getAnhDaiDien();
        if (file != null && !file.isEmpty()) {
            anhDaiDienPath = saveAvatar(file);
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
                .trangThai(TrangThaiTaiKhoan.ACTIVE)
                .loaiTaiKhoan(new HashSet<>())
                .build();

        LoaiTaiKhoan defaultRole = loaiTaiKhoanRepository.findByTenLoaiTK("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Khong tim thay role mac dinh"));
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

    private String saveAvatar(MultipartFile file) {
        try {
            Path uploadDirPath = resolveAvatarUploadDir();
            Files.createDirectories(uploadDirPath);

            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar.jpg";
            String fileName = "avatar_" + System.currentTimeMillis() + "_"
                    + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");

            Path uploadPath = uploadDirPath.resolve(fileName);
            file.transferTo(uploadPath.toFile());
            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Loi khi luu anh dai dien: " + e.getMessage(), e);
        }
    }

    private Path resolveAvatarUploadDir() {
        Path[] candidates = new Path[]{
                Paths.get("petshop", "src", "main", "resources", "static", "uploads", "avatars"),
                Paths.get("src", "main", "resources", "static", "uploads", "avatars")
        };

        for (Path candidate : candidates) {
            Path absoluteCandidate = candidate.toAbsolutePath().normalize();
            Path staticDir = absoluteCandidate.getParent() != null
                    ? absoluteCandidate.getParent().getParent()
                    : null;

            if (staticDir != null && Files.exists(staticDir)) {
                return absoluteCandidate;
            }
        }

        return candidates[0].toAbsolutePath().normalize();
    }
}
