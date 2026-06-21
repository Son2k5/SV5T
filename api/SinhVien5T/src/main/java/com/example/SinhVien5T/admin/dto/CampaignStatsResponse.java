package com.example.SinhVien5T.admin.dto;

public record CampaignStatsResponse(
        Long campaignId,
        long totalApplications,
        long submittedApplications,
        long approvedApplications,
        long rejectedApplications,
        long pendingEvidences
) {}
