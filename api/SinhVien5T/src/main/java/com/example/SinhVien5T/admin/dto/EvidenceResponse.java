package com.example.SinhVien5T.admin.dto;

public record EvidenceResponse(
        String publicId,
        String evidenceUrl,
        String evidenceOriginalFilename,
        String evidenceFormat,
        String status,
        String reviewerComment,
        String criteriaPublicId,
        String criteriaName,
        String userPublicId,
        String userEmail,
        String studentName,
        String campaignPublicId,
        String campaignName
) {}
