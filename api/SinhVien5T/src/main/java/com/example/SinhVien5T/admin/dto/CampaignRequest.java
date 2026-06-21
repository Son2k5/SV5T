package com.example.SinhVien5T.admin.dto;

import java.time.LocalDate;

import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Level;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CampaignRequest(
        @NotBlank String name,
        @NotNull @Positive Long academicYear,
        @NotNull Level level,
        LocalDate startDate,
        LocalDate endDate,
        CampaignStatus status
) {
        @AssertTrue(message = "endDate must be on or after startDate")
        public boolean isValidDateRange() {
                return startDate == null || endDate == null || !endDate.isBefore(startDate);
        }
}
