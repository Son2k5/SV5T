package com.example.SinhVien5T.admin.dto;

import java.time.LocalDate;

public record CampaignResponse(
        Long id,
        String publicId,
        String name,
        Long academicYear,
        String level,
        String status,
        LocalDate startDate,
        LocalDate endDate
) {}
