package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.Level;
import java.time.LocalDateTime;

public record ResultResponse(
        String applicationPublicId,
        String userPublicId,
        String userEmail,
        String studentName,
        String studentCode,
        String campaignPublicId,
        String campaignName,
        Level level,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int totalEvidences
) {}
