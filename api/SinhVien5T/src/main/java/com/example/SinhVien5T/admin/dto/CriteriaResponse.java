package com.example.SinhVien5T.admin.dto;

public record CriteriaResponse (
    Long id,
        String name,
        String description,
        Boolean mandatory,
        Integer requiredChildrenCount,
        String evidenceType,
        Long parentId){
}
