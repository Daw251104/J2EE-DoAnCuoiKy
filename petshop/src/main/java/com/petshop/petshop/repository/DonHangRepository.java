package com.petshop.petshop.repository;

import com.petshop.petshop.model.DonHang;
import com.petshop.petshop.model.TaiKhoan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {

    List<DonHang> findByKhachHangOrderByNgayLapDonHangDesc(TaiKhoan khachHang);

    long countByTrangThai(String trangThai);

    List<DonHang> findTop8ByOrderByNgayLapDonHangDesc();

    @Query("select sum(d.tongTien) from DonHang d where d.trangThai = :trangThai")
    BigDecimal tongDoanhThuByTrangThai(@Param("trangThai") String trangThai);

    @Query("""
           select sum(d.tongTien)
           from DonHang d
           where d.trangThai = :trangThai
             and d.ngayLapDonHang >= :fromDate
             and d.ngayLapDonHang < :toDate
           """)
    BigDecimal tongDoanhThuByTrangThaiVaKhoangNgay(@Param("trangThai") String trangThai,
                                                    @Param("fromDate") LocalDateTime fromDate,
                                                    @Param("toDate") LocalDateTime toDate);
}
