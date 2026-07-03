package com.example.SinhVien5T.notification.controller;

import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.notification.dto.NotificationSettingRequest;
import com.example.SinhVien5T.notification.dto.NotificationSettingResponse;
import com.example.SinhVien5T.notification.dto.NotificationTemplateDto;
import com.example.SinhVien5T.notification.dto.NotificationTemplateRequest;
import com.example.SinhVien5T.notification.dto.SmtpTestRequest;
import com.example.SinhVien5T.notification.entity.NotificationType;
import com.example.SinhVien5T.notification.service.NotificationSettingService;
import com.example.SinhVien5T.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin notifications", description = "Cau hinh notification va template thong bao")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('MANAGE_NOTIFICATION_SETTINGS')")
public class AdminNotificationController {

    private final NotificationSettingService settingService;
    private final NotificationTemplateService templateService;

    @GetMapping("/notification-settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getSettings() {
        return ok("Lay cau hinh notification thanh cong", settingService.getSettings());
    }

    @PutMapping("/notification-settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateSettings(
            @Valid @RequestBody NotificationSettingRequest request,
            Authentication authentication
    ) {
        return ok("Cap nhat cau hinh notification thanh cong", settingService.updateSettings(request, authentication));
    }

    @PostMapping("/notification-settings/test")
    public ResponseEntity<ApiResponse<Void>> testSmtp(
            @Valid @RequestBody SmtpTestRequest request,
            Authentication authentication
    ) {
        settingService.testSmtpConnection(request, authentication);
        return ok("Gui email test thanh cong", null);
    }

    @GetMapping("/notification-templates")
    public ResponseEntity<ApiResponse<List<NotificationTemplateDto>>> listTemplates() {
        return ok("Lay danh sach template notification thanh cong", templateService.listTemplates());
    }

    @PutMapping("/notification-templates/{code}")
    public ResponseEntity<ApiResponse<NotificationTemplateDto>> updateTemplate(
            @PathVariable NotificationType code,
            @Valid @RequestBody NotificationTemplateRequest request
    ) {
        return ok("Cap nhat template notification thanh cong", templateService.updateTemplate(code, request));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}
