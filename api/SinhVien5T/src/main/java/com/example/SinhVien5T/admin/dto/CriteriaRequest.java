package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.EvidenceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriteriaRequest(
        @NotBlank(message = "Tên tiêu chí không được để trống") @Size(max = 500, message = "Tên tiêu chí không được vượt quá 500 ký tự") String name,
        String description,
        @Min(value = 1, message = "Thứ tự tiêu chí phải lớn hơn 0") Integer orderIndex,
        Boolean mandatory,
        @Min(value = 0, message = "Số tiêu chí con bắt buộc không được nhỏ hơn 0") Integer requiredChildrenCount,
        EvidenceType evidenceType,
        String parentPublicId
) {}
