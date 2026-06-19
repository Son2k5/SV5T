package com.example.SinhVien5T.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveEvidenceRequest {

    @NotNull(message = "Tiêu chí không được để trống")
    private Long criteriaId;

    @NotBlank(message = "Minh chứng không được để trống")
    @Size(max = 7_000_000, message = "Minh chứng không được vượt quá 5MB")
    private String evidenceUrl;
}
