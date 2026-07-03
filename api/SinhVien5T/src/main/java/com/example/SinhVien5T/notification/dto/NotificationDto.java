package com.example.SinhVien5T.notification.dto;

import com.example.SinhVien5T.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        String publicId,
        String title,
        String content,
        NotificationType type,
        boolean read,
        LocalDateTime createdAt,
        String relatedEntityType,
        String relatedEntityId
) {
}
