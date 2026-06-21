package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.EvidenceType;

import jakarta.validation.constraints.Min;

public record CriteriaRequirementRequest (Boolean mandatory,
        @Min(0) Integer requiredChildrenCount,
        EvidenceType evidenceType){
    
}
