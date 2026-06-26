package com.example.SinhVien5T.campaign.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationRecordResponse {
    private String publicId;
    private String campaignPublicId;
    private String campaignName;
    private String level;
    private String status;
    private String note;
    private Boolean isGroup;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}