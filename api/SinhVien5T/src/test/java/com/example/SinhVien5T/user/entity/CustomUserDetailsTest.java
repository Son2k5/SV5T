package com.example.SinhVien5T.user.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void getAuthorities_shouldReturnRoleAuthorityWhenPermissionsAreEmpty() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                "user-pub-123",
                "test@example.com",
                "hashedpwd",
                Role.ADMIN,
                true,
                List.of()
        );

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void getAuthorities_shouldReturnCombinedRoleAndFineGrainedPermissions() {
        List<String> permissions = List.of("USER_MANAGE", "EVIDENCE_REVIEW");
        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                "user-pub-123",
                "test@example.com",
                "hashedpwd",
                Role.ADMIN,
                true,
                permissions
        );

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertEquals(3, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("USER_MANAGE")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("EVIDENCE_REVIEW")));
    }
}
