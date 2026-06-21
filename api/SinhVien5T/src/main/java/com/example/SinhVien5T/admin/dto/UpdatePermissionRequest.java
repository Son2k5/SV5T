package com.example.SinhVien5T.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UpdatePermissionRequest (@NotEmpty List<@NotBlank String> permissions){
    
}
