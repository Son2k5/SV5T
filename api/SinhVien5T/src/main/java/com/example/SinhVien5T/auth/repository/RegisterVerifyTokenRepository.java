package com.example.SinhVien5T.auth.repository;

import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.auth.entity.RegisterVerifyToken;
import com.example.SinhVien5T.auth.entity.TokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegisterVerifyTokenRepository extends JpaRepository<RegisterVerifyToken, String> {

    Optional<RegisterVerifyToken> findByToken(String token);

    Optional<RegisterVerifyToken> findByTokenAndPurpose(String token, TokenPurpose purpose);

    List<RegisterVerifyToken> findByUserId(Long userId);

    List<RegisterVerifyToken> findByUserIdAndPurpose(Long userId, TokenPurpose purpose);

    List<RegisterVerifyToken> findByExpiryDateBefore(LocalDateTime now);

    void deleteByExpiryDateBefore(LocalDateTime now);

    void deleteByUser(User user);

    void deleteByUserAndPurpose(User user, TokenPurpose purpose);
}


