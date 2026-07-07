package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.EvidenceType;
import com.example.SinhVien5T.campaign.entity.Level;

public record CriteriaTemplateResponse(
        String publicId,
        String name,
        String description,
        Boolean mandatory,
        EvidenceType evidenceType,
        Level level
) {}
