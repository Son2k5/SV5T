package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.user.entity.Role;

import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull(message = "Vai trò không được để trống") Role role) {
    
}
