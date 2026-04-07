package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.UpdateProfileRequest;
import com.petshop.petshop.model.TaiKhoan;
import com.petshop.petshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public TaiKhoan layThongTinTaiKhoan(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan: " + username));
    }

    @Transactional
    public void capNhatThongTin(String username, UpdateProfileRequest request) {
        TaiKhoan taiKhoan = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan: " + username));

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()
                && !request.getEmail().equals(taiKhoan.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email nay da duoc su dung boi tai khoan khac!");
            }
            taiKhoan.setEmail(request.getEmail());
        }

        taiKhoan.setHoTen(request.getHoTen());
        taiKhoan.setSdt(request.getSdt());
        taiKhoan.setGioiTinh(request.getGioiTinh());
        taiKhoan.setDiaChi(request.getDiaChi());

        if (request.getFileAnhDaiDien() != null && !request.getFileAnhDaiDien().isEmpty()) {
            taiKhoan.setAnhDaiDien(saveImage(request.getFileAnhDaiDien()));
        } else if (request.getAnhDaiDien() != null && !request.getAnhDaiDien().trim().isEmpty()) {
            taiKhoan.setAnhDaiDien(request.getAnhDaiDien());
        }

        userRepository.save(taiKhoan);
    }

    @Transactional
    public void doiMatKhau(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            throw new RuntimeException("Vui long nhap day du thong tin doi mat khau");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Xac nhan mat khau moi khong khop");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("Mat khau moi phai co it nhat 6 ky tu");
        }

        TaiKhoan taiKhoan = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan: " + username));

        if (!passwordEncoder.matches(currentPassword, taiKhoan.getPassword())) {
            throw new RuntimeException("Mat khau hien tai khong dung");
        }
        if (passwordEncoder.matches(newPassword, taiKhoan.getPassword())) {
            throw new RuntimeException("Mat khau moi khong duoc trung voi mat khau cu");
        }

        taiKhoan.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(taiKhoan);
    }

    @Transactional
    public void datLaiMatKhauTheoUsernameVaEmail(String username, String email, String newPassword, String confirmPassword) {
        if (isBlank(username) || isBlank(email) || isBlank(newPassword) || isBlank(confirmPassword)) {
            throw new RuntimeException("Vui long nhap day du thong tin");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Xac nhan mat khau moi khong khop");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("Mat khau moi phai co it nhat 6 ky tu");
        }

        TaiKhoan taiKhoan = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan voi username da nhap"));

        String emailDb = taiKhoan.getEmail() == null ? "" : taiKhoan.getEmail().trim();
        if (!emailDb.equalsIgnoreCase(email.trim())) {
            throw new RuntimeException("Email khong khop voi tai khoan nay");
        }

        if (passwordEncoder.matches(newPassword, taiKhoan.getPassword())) {
            throw new RuntimeException("Mat khau moi khong duoc trung voi mat khau cu");
        }

        taiKhoan.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(taiKhoan);
    }

    private String saveImage(MultipartFile file) {
        try {
            Path uploadDirPath = resolveAvatarUploadDir();
            Files.createDirectories(uploadDirPath);

            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar.jpg";
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = "avatar_" + System.currentTimeMillis() + "_" + safeFilename;

            Path uploadPath = uploadDirPath.resolve(fileName);
            file.transferTo(uploadPath.toFile());

            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Loi khi luu hinh anh dai dien: " + e.getMessage(), e);
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
