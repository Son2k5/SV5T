package com.example.SinhVien5T.admin.dto;

import java.util.List;

public record ResultDetailResponse(
        String userPublicId,
        String campaignPublicId,
        String campaignName,
        String status,
        List<CriteriaResult> criteriaResults
) {
    public record CriteriaResult(
            String criteriaPublicId,
            String criteriaName,
            Boolean mandatory,
            String evidenceType,
            String evidenceStatus,
            String evidenceUrl,
            String reviewerComment
    ) {}
}
