package com.example.SinhVien5T.notification.repository;

import com.example.SinhVien5T.notification.entity.NotificationTemplate;
import com.example.SinhVien5T.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByPublicId(String publicId);

    Optional<NotificationTemplate> findByCode(NotificationType code);

    Optional<NotificationTemplate> findByCodeAndActiveTrue(NotificationType code);

    boolean existsByCode(NotificationType code);

    List<NotificationTemplate> findAllByOrderByCodeAsc();
}
