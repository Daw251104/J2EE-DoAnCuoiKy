package com.petshop.petshop.service.service;

import com.petshop.petshop.dto.UpdateProfileRequest;
import com.petshop.petshop.model.TaiKhoan;
import com.petshop.petshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy thông tin tài khoản theo username.
     */
    public TaiKhoan layThongTinTaiKhoan(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));
    }

    /**
     * Cập nhật thông tin cá nhân.
     */
    @Transactional
    public void capNhatThongTin(String username, UpdateProfileRequest request) {
        TaiKhoan taiKhoan = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));

        // Nếu người dùng đổi email, cần kiểm tra xem email mới có trùng với email của người khác hay không
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()
                && !request.getEmail().equals(taiKhoan.getEmail())) {
            
            // Check email trùng
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
            }
            taiKhoan.setEmail(request.getEmail());
        }

        taiKhoan.setHoTen(request.getHoTen());
        taiKhoan.setSdt(request.getSdt());
        taiKhoan.setGioiTinh(request.getGioiTinh());
        taiKhoan.setDiaChi(request.getDiaChi());
        
        // Cập nhật ảnh đại diện nếu có
        if (request.getFileAnhDaiDien() != null && !request.getFileAnhDaiDien().isEmpty()) {
            taiKhoan.setAnhDaiDien(saveImage(request.getFileAnhDaiDien()));
        } else if (request.getAnhDaiDien() != null && !request.getAnhDaiDien().trim().isEmpty()) {
            taiKhoan.setAnhDaiDien(request.getAnhDaiDien());
        }

        userRepository.save(taiKhoan);
    }

    private String saveImage(MultipartFile file) {
        try {
            String projectRoot = System.getProperty("user.dir");
            String relativePath = "/src/main/resources/static/uploads/avatars/";
            String uploadDir = projectRoot + relativePath;

            Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadDirPath);

            String originalFilename = file.getOriginalFilename();
            String safeFilename = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "avatar.jpg";
            String fileName = "avatar_" + System.currentTimeMillis() + "_" + safeFilename;

            Path uploadPath = uploadDirPath.resolve(fileName);
            file.transferTo(uploadPath.toFile());

            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu hình ảnh đại diện: " + e.getMessage(), e);
        }
    }
}
