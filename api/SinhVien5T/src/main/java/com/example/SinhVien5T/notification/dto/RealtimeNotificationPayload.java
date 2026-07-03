package com.example.SinhVien5T.notification.dto;

public record RealtimeNotificationPayload(
        NotificationDto notification,
        long unreadCount
) {
}
