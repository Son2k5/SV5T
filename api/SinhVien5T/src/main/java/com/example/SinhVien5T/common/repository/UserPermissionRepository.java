package com.example.SinhVien5T.common.repository;

import com.example.SinhVien5T.common.entity.UserPermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    @EntityGraph(attributePaths = "permission")
    List<UserPermission> findByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    List<UserPermission> findByPermissionCode(String permissionCode);

    @EntityGraph(attributePaths = {"user", "permission"})
    Optional<UserPermission> findByUserIdAndPermissionCode(Long userId, String permissionCode);

    boolean existsByUserIdAndPermissionCode(Long userId, String permissionCode);

    void deleteByUserIdAndPermissionCode(Long userId, String permissionCode);
}
