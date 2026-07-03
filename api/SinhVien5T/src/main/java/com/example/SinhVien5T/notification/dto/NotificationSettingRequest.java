package com.example.SinhVien5T.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record NotificationSettingRequest(
        String smtpHost,

        @Min(value = 1, message = "SMTP port phai lon hon 0")
        @Max(value = 65535, message = "SMTP port khong hop le")
        Integer smtpPort,

        String smtpUsername,
        String smtpPassword,
        Boolean emailEnabled,
        Boolean realtimeEnabled,

        @Min(value = 0, message = "So ngay nhac han khong duoc am")
        @Max(value = 30, message = "So ngay nhac han khong duoc vuot qua 30")
        Integer reminderDaysBeforeDeadline
) {
}
