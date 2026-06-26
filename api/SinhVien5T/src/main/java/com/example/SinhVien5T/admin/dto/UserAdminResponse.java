package com.example.SinhVien5T.admin.dto;

import java.time.LocalDateTime;

import com.example.SinhVien5T.user.entity.Role;

public record UserAdminResponse(
        String publicId,
        String email,
        Role role,
        Boolean active,
        Boolean verified,
        String fullName,
        String studentCode,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
