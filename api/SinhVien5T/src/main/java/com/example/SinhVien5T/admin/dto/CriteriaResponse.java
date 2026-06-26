package com.example.SinhVien5T.admin.dto;

public record CriteriaResponse (
    String publicId,
        String name,
        String description,
        Integer orderIndex,
        Boolean mandatory,
        Integer requiredChildrenCount,
        String evidenceType,
        String parentPublicId){
}
