package com.example.SinhVien5T.auth.repository;

import com.example.SinhVien5T.auth.entity.RefreshToken;
import com.example.SinhVien5T.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    public Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByToken(String token);

    @Modifying
    @Transactional
    void deleteByExpiredAtBefore(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("update RefreshToken rt set rt.isRevoked = true where rt.user = :user and (rt.isRevoked = false or rt.isRevoked is null)")
    int revokeAllByUser(User user);

}


