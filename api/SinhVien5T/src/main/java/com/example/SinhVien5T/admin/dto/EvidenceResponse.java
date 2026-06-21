package com.example.SinhVien5T.admin.dto;

public record EvidenceResponse(
        Long id,
        String evidenceUrl,
        String evidenceOriginalFilename,
        String evidenceFormat,
        String status,
        String reviewerComment,
        Long criteriaId,
        String criteriaName,
        Long userId,
        String userEmail,
        String studentName,
        Long campaignId,
        String campaignName
) {}
