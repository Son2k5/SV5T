package com.example.SinhVien5T.admin.dto;

public record CampaignStatsResponse(
        String campaignPublicId,
        long totalParticipants,
        long pendingResults,
        long passResults,
        long failResults,
        long pendingEvidences,
        long approvedEvidences,
        long rejectedEvidences,
        double passRate,
        double failRate
) {}
