package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.user.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
        @Email @NotBlank @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Role role,
        @Size(max = 100) String fullName,
        @Size(max = 50) String studentCode,
        @Size(max = 20) String phoneNumber
) {}
