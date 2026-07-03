package com.example.SinhVien5T.common.controller;

import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.common.entity.SystemSetting;
import com.example.SinhVien5T.common.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/settings")
@RequiredArgsConstructor
public class PublicSystemSettingController {

    private static final List<String> PUBLIC_KEYS = List.of(
            "system.name",
            "system.shortName",
            "system.logoUrl",
            "system.faviconUrl",
            "system.homeBannerUrl",
            "system.contactEmail",
            "system.hotline",
            "system.address",
            "system.timezone",
            "system.defaultLanguage",
            "system.maintenance.enabled",
            "system.maintenance.message",
            "system.footerText"
    );

    private final SystemSettingRepository settingRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicSettings() {
        Map<String, String> settings = settingRepository.findAllById(PUBLIC_KEYS)
                .stream()
                .collect(Collectors.toMap(SystemSetting::getKeyName, SystemSetting::getValue));

        return ResponseEntity.ok(ApiResponse.success("Lấy cấu hình public thành công", settings));
    }
}
