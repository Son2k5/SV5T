package com.example.SinhVien5T.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationRecordRequest {

    @NotBlank(message = "Mã đợt xét chọn không được để trống")
    private String campaignPublicId;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;

    private Boolean isGroup = false;

    private String level;
}