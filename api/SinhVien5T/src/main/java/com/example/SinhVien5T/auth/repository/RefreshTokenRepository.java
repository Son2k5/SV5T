package com.example.SinhVien5T.auth.repository;

import com.example.SinhVien5T.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(Long userId);

    List<RefreshToken> findByUserIdAndIsRevoked(Long userId, Boolean isRevoked);

    @Modifying
    @Transactional
    void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query("delete from RefreshToken rt where rt.expiredAt < :now")
    int deleteByExpiredAtBefore(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("update RefreshToken rt set rt.isRevoked = true where rt.user.id = :userId and (rt.isRevoked = false or rt.isRevoked is null)")
    int revokeAllByUserId(@Param("userId") Long userId);

}


