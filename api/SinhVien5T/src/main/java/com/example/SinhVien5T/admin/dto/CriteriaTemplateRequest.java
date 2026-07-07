package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.EvidenceType;
import com.example.SinhVien5T.campaign.entity.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriteriaTemplateRequest(
        @NotBlank(message = "Tên tiêu chí mẫu không được để trống")
        @Size(max = 500, message = "Tên tiêu chí mẫu không được vượt quá 500 ký tự")
        String name,
        String description,
        Boolean mandatory,
        EvidenceType evidenceType,
        Level level
) {}
