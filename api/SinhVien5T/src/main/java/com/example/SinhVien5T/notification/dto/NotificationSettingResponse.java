package com.example.SinhVien5T.notification.dto;

import java.time.LocalDateTime;

public record NotificationSettingResponse(
        String publicId,
        String smtpHost,
        Integer smtpPort,
        String smtpUsername,
        String smtpPassword,
        boolean passwordConfigured,
        boolean emailEnabled,
        boolean realtimeEnabled,
        Integer reminderDaysBeforeDeadline,
        String updatedBy,
        LocalDateTime updatedAt
) {
}
