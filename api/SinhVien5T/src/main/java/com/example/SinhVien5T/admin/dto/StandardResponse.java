package com.example.SinhVien5T.admin.dto;

public record StandardResponse(
        String publicId,
        String name,
        String description,
        String campaignPublicId,
        Long criteriaCount,
        Boolean isGroup,
        String level
) {}
