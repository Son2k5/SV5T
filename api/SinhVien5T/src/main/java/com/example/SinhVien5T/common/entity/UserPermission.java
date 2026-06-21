package com.example.SinhVien5T.common.entity;

import com.example.SinhVien5T.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_permissions",
        indexes = {
                @Index(name = "idx_user_permissions_permission_code", columnList = "permission_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_permissions_user_permission",
                        columnNames = {"user_id", "permission_code"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "permission_code",
            referencedColumnName = "code",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_permissions_permission")
    )
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_permissions_user")
    )
    private User user;
}
