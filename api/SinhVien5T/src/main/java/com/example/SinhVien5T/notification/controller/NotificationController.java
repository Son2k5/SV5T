package com.example.SinhVien5T.notification.controller;

import com.example.SinhVien5T.admin.dto.PageResponse;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.notification.dto.NotificationDto;
import com.example.SinhVien5T.notification.entity.NotificationType;
import com.example.SinhVien5T.notification.service.NotificationService;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Thong bao cua nguoi dung hien tai")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) NotificationType type,
            Authentication authentication
    ) {
        return ok(
                "Lay danh sach thong bao thanh cong",
                notificationService.getNotificationHistory(
                        currentUser(authentication).getId(),
                        type,
                        PageRequest.of(page, size)
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        return ok(
                "Lay so thong bao chua doc thanh cong",
                notificationService.getUnreadCount(currentUser(authentication).getId())
        );
    }

    @PatchMapping("/{notificationPublicId}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(
            @PathVariable String notificationPublicId,
            Authentication authentication
    ) {
        return ok(
                "Danh dau thong bao da doc thanh cong",
                notificationService.markAsRead(notificationPublicId, currentUser(authentication).getId())
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(currentUser(authentication).getId());
        return ok("Danh dau tat ca thong bao da doc thanh cong", null);
    }

    private CustomUserDetails currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new IllegalArgumentException("Nguoi dung chua dang nhap");
        }
        return user;
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}
