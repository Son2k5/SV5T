package com.example.SinhVien5T.campaign.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class CampaignSummaryResponse {

    private String publicId;
    private String name;
    private Long academicYear;
    private String level;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String isGroup;
}
