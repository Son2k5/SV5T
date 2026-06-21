package com.example.SinhVien5T.user.repository;

import com.example.SinhVien5T.user.entity.UserAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    Page<UserAddress> findByProvinceCity(String provinceCity, Pageable pageable);

    Page<UserAddress> findByDistrict(String district, Pageable pageable);

    Page<UserAddress> findByProvinceCityAndDistrict(
            String provinceCity,
            String district,
            Pageable pageable
    );
}
