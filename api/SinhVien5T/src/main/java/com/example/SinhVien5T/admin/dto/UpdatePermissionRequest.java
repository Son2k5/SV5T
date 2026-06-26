package com.example.SinhVien5T.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UpdatePermissionRequest (@NotEmpty(message = "Danh sách quyền không được để trống") List<@NotBlank(message = "Quyền không được để trống") String> permissions){
    
}
