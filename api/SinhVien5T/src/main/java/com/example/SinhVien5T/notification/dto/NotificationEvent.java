package com.example.SinhVien5T.notification.dto;

import com.example.SinhVien5T.notification.entity.NotificationType;

import java.util.Map;

public record NotificationEvent(
        Long recipientId,
        NotificationType type,
        Map<String, String> payload,
        String relatedEntityType,
        String relatedEntityId
) {
}
