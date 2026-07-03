package com.example.SinhVien5T.notification.dto;

import com.example.SinhVien5T.notification.entity.NotificationChannel;
import com.example.SinhVien5T.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationTemplateDto(
        String publicId,
        NotificationType code,
        String subject,
        String bodyTemplate,
        NotificationChannel channel,
        boolean active,
        LocalDateTime updatedAt
) {
}
