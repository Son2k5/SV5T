package com.example.SinhVien5T.user.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.SinhVien5T.common.entity.UserPermission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String publicId;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean active;
    private final List<String> permissions;

    public CustomUserDetails(Long id, String publicId, String email, String passwordHash, Role role, boolean active) {
        this(id, publicId, email, passwordHash, role, active, List.of());
    }

    public CustomUserDetails(
            Long id,
            String publicId,
            String email,
            String passwordHash,
            Role role,
            boolean active,
            Collection<String> permissions
    ) {
        this.id = id;
        this.publicId = publicId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }

    public static CustomUserDetails from(User user) {
        return from(user, List.of());
    }

    public static CustomUserDetails from(User user, Collection<UserPermission> permissions) {
        return new CustomUserDetails(
                user.getId(),
                user.getPublicId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.isActive(),
                permissions.stream()
                        .map(UserPermission::getPermission)
                        .filter(permission -> permission != null && permission.getCode() != null)
                        .map(permission -> permission.getCode())
                        .toList()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
