package com.example.SinhVien5T.common.security;

import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.Role;

import java.util.List;

public record CachedUserPrincipal(
        Long id,
        String publicId,
        String email,
        Role role,
        boolean active,
        List<String> permissions
) {

    public CustomUserDetails toUserDetails() {
        return new CustomUserDetails(
                id,
                publicId,
                email,
                null,
                role,
                active,
                permissions
        );
    }
}
