package com.example.SinhVien5T.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record IdsRequest (@NotEmpty(message = "Danh sách ID không được để trống") List<String> publicIds) {
    
}
