package com.example.SinhVien5T.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record IdsRequest (@NotEmpty List<@NotNull Long> ids) {
    
}
