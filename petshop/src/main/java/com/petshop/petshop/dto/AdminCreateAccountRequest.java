package com.petshop.petshop.dto;

import com.petshop.petshop.model.GioiTinh;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AdminCreateAccountRequest {

    private String username;
    private String password;
    private String hoTen;

    @Enumerated(EnumType.STRING)
    private GioiTinh gioiTinh;

    private String sdt;
    private String email;
    private String diaChi;

    private MultipartFile anhDaiDien;

    /** Danh sách maLoaiTK được chọn từ form */
    private List<Integer> loaiTaiKhoans;
}
