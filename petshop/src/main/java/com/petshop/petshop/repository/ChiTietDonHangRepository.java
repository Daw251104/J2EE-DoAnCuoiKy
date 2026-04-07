package com.petshop.petshop.repository;

import com.petshop.petshop.dto.AdminTopProductDTO;
import com.petshop.petshop.model.ChiTietDonHang;
import com.petshop.petshop.model.DonHang;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {

    List<ChiTietDonHang> findByDonHang(DonHang donHang);

    @Query("select sum(ct.sl) from ChiTietDonHang ct where ct.donHang.trangThai = :trangThai")
    Long tongSoLuongTheoTrangThai(@Param("trangThai") String trangThai);

    @Query("""
           select new com.petshop.petshop.dto.AdminTopProductDTO(
               ct.sanPham.maSP,
               ct.sanPham.tenSP,
               ct.sanPham.hinhDaiDien,
               sum(ct.sl),
               sum(ct.giaBanRa * ct.sl)
           )
           from ChiTietDonHang ct
           where ct.donHang.trangThai = :trangThai
           group by ct.sanPham.maSP, ct.sanPham.tenSP, ct.sanPham.hinhDaiDien
           order by sum(ct.sl) desc
           """)
    List<AdminTopProductDTO> thongKeTopSanPhamTheoTrangThai(@Param("trangThai") String trangThai, Pageable pageable);
}
