package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StandardRequest(
        @NotBlank(message = "Tên tiêu chuẩn không được để trống") @Size(max = 500, message = "Tên tiêu chuẩn không được vượt quá 500 ký tự") String name,
        String description,
        Boolean isGroup,
        Level level
) {}
