package com.example.SinhVien5T.admin.dto;

import java.time.LocalDate;

import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Level;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CampaignRequest(
        @NotBlank(message = "Tên đợt xét chọn không được để trống") String name,
        @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự") String description,
        @NotNull(message = "Năm học không được để trống") @Positive(message = "Năm học phải lớn hơn 0") Long academicYear,
        @NotNull(message = "Cấp xét chọn không được để trống") Level level,
        LocalDate startDate,
        LocalDate endDate,
        CampaignStatus status,
        String isGroup
) {
        @AssertTrue(message = "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu")
        public boolean isValidDateRange() {
                return startDate == null || endDate == null || !endDate.isBefore(startDate);
        }
}
