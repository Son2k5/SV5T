package com.example.SinhVien5T.notification.repository;

import com.example.SinhVien5T.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByPublicId(String publicId);

    Optional<NotificationSetting> findFirstByOrderByUpdatedAtDesc();
}
