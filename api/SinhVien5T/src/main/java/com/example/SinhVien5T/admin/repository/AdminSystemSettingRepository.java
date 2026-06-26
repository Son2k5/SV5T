package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.common.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSystemSettingRepository extends JpaRepository<SystemSetting, String> {
}