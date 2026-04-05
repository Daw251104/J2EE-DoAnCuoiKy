package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.UpdateProfileRequest;
import com.petshop.petshop.model.TaiKhoan;
import com.petshop.petshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
