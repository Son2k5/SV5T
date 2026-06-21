package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.EvidenceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriteriaRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        Boolean mandatory,
        @Min(0) Integer requiredChildrenCount,
        EvidenceType evidenceType,
        Long parentId
) {}
