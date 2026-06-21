package com.example.SinhVien5T.auth.repository;

import com.example.SinhVien5T.auth.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, String> {

    Optional<Otp> findByOtp(String otp);

    Optional<Otp> findFirstByEmailAndExpiredAtAfterOrderByExpiredAtDesc(
            String email,
            LocalDateTime now
    );

    void deleteByExpiredAtBefore(LocalDateTime now);
}


