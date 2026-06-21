package com.example.SinhVien5T.common.security;

import com.example.SinhVien5T.common.config.CacheConfig;
import com.example.SinhVien5T.common.repository.UserPermissionRepository;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CachedUserPrincipalService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Cacheable(cacheNames = CacheConfig.USER_AUTH, key = "#userId", sync = true)
    @Transactional(readOnly = true)
    public CachedUserPrincipal loadByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new JwtException("User not found"));

        return new CachedUserPrincipal(
                user.getId(),
                user.getPublicId(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                userPermissionRepository.findByUserId(user.getId())
                        .stream()
                        .map(userPermission -> userPermission.getPermission())
                        .filter(permission -> permission != null && permission.getCode() != null)
                        .map(permission -> permission.getCode())
                        .toList()
        );
    }

    @CacheEvict(cacheNames = CacheConfig.USER_AUTH, key = "#userId")
    public void evict(Long userId) {
        // Annotation-driven eviction hook.
    }
}
