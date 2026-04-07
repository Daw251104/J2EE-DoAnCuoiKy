package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.AdminCreateAccountRequest;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<TaiKhoan> getAllAccounts() {
        return userRepository.findAll();
    }

    public List<TaiKhoan> getAccountsByFilters(String keyword,
                                               String roleName,
                                               TrangThaiTaiKhoan trangThai) {
        String normalizedKeyword = normalize(keyword);
        String normalizedRoleName = normalize(roleName);

        return userRepository.findAll().stream()
                .filter(account -> matchKeyword(account, normalizedKeyword))
                .filter(account -> matchRole(account, normalizedRoleName))
                .filter(account -> matchStatus(account, trangThai))
                .toList();
    }

    public List<LoaiTaiKhoan> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public TaiKhoan createAccount(AdminCreateAccountRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Ten dang nhap da ton tai");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email da ton tai");
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
                        .orElseThrow(() -> new RuntimeException("Khong tim thay role ID: " + maLoaiTK));
                selectedRoles.add(role);
            }
        } else {
            LoaiTaiKhoan defaultRole = roleRepository.findByTenLoaiTK("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Khong tim thay role mac dinh CUSTOMER"));
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

    @Transactional
    public TaiKhoan updateStatus(Integer maTK, TrangThaiTaiKhoan trangThai) {
        TaiKhoan taiKhoan = userRepository.findById(maTK)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan ID: " + maTK));
        taiKhoan.setTrangThai(trangThai);
        return userRepository.save(taiKhoan);
    }

    private boolean matchKeyword(TaiKhoan account, String normalizedKeyword) {
        if (normalizedKeyword.isBlank()) {
            return true;
        }

        return normalize(account.getUsername()).contains(normalizedKeyword)
                || normalize(account.getHoTen()).contains(normalizedKeyword)
                || normalize(account.getEmail()).contains(normalizedKeyword);
    }

    private boolean matchRole(TaiKhoan account, String normalizedRoleName) {
        if (normalizedRoleName.isBlank()) {
            return true;
        }
        if (account.getLoaiTaiKhoan() == null || account.getLoaiTaiKhoan().isEmpty()) {
            return false;
        }

        return account.getLoaiTaiKhoan().stream()
                .map(role -> normalize(role.getTenLoaiTK()))
                .anyMatch(role -> role.equals(normalizedRoleName));
    }

    private boolean matchStatus(TaiKhoan account, TrangThaiTaiKhoan trangThai) {
        return trangThai == null || trangThai == account.getTrangThai();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String saveAvatar(MultipartFile file) {
        try {
            Path uploadDirPath = resolveAvatarUploadDir();
            Files.createDirectories(uploadDirPath);

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar.jpg";
            String fileName = "avatar_" + System.currentTimeMillis() + "_"
                    + originalName.replaceAll("[^a-zA-Z0-9.-]", "_");

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
