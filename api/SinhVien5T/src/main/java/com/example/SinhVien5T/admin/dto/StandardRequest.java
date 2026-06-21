package com.example.SinhVien5T.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StandardRequest(
        @NotBlank @Size(max = 255) String name,
        String description
) {}
