package com.example.SinhVien5T.common.repository;

import com.example.SinhVien5T.common.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, String> {

    List<Permission> findByNameContainingIgnoreCase(String name);
}
