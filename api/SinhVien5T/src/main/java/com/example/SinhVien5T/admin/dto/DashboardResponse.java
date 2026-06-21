package com.example.SinhVien5T.admin.dto;

public record DashboardResponse(
        long totalUsers,
        long activeCampaigns,
        long pendingEvidences,
        Object topStudents
) {}