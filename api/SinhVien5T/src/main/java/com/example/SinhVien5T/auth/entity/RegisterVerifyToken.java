package com.example.SinhVien5T.auth.entity;

import com.example.SinhVien5T.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "register_verify_token",
        indexes = {
                @Index(name = "idx_register_verify_token_user_id", columnList = "user_id"),
                @Index(name = "idx_register_verify_token_expiry_date", columnList = "expiry_date"),
                @Index(name = "idx_register_verify_token_purpose", columnList = "purpose"),
                @Index(name = "idx_register_verify_token_user_purpose", columnList = "user_id, purpose")
        }
)
public class RegisterVerifyToken {

    @Id
    private String token; // UUID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expiry_date")
    LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TokenPurpose purpose;

}


