package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.campaign.entity.CampaignStatus;

import jakarta.validation.constraints.NotNull;

public record CampaignStatusRequest(@NotNull(message = "Trạng thái đợt xét chọn không được để trống") CampaignStatus status) {}
