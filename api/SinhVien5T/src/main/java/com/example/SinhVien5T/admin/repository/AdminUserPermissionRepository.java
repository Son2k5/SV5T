package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.common.entity.UserPermission;
import org.springframework.data.jpa.repository.*;

import java.util.List;

public interface AdminUserPermissionRepository extends JpaRepository<UserPermission, Long> {
    @EntityGraph(attributePaths = "permission")
    List<UserPermission> findByUserId(Long userId);
}