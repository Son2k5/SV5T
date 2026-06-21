package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.user.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
        @Email @Size(max = 100) String email,
        Role role,
        Boolean active,
        @Size(max = 100) String fullName,
        @Size(max = 50) String studentCode,
        @Size(max = 20) String phoneNumber
) {}
