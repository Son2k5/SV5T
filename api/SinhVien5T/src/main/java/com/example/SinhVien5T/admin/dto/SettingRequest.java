package com.example.SinhVien5T.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SettingRequest(
        @Size(max = 100, message = "Mã cấu hình không được vượt quá 100 ký tự") String keyName,
        @NotNull(message = "Giá trị cấu hình không được để trống") String value,
        String description
) {}
