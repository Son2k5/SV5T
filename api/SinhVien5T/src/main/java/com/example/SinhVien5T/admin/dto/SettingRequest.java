package com.example.SinhVien5T.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SettingRequest(
        @NotBlank @Size(max = 100) String keyName,
        @NotNull String value,
        String description
) {}
