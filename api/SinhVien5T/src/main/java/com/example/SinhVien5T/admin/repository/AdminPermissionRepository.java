package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.common.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminPermissionRepository extends JpaRepository<Permission, String> {
}