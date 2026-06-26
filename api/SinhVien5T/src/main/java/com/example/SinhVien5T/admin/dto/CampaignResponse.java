package com.example.SinhVien5T.admin.dto;

import java.time.LocalDate;

public record CampaignResponse(
        String publicId,
        String name,
        String description,
        Long academicYear,
        String level,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Long criteriaCount,
        String isGroup
) {}
