package com.example.SinhVien5T.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
public class TokenStateService {

    private static final String VERSION_FIELD = "version";
    private static final String REVOKED_BEFORE_FIELD = "revokedBeforeEpochMs";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.redis.key-prefix:sv5t}")
    private String keyPrefix;

    @Value("${app.jwt.access-expiration:1800000}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    @Value("${app.redis.token.enabled:true}")
    private boolean redisTokenEnabled;

    public TokenStateService(
            @Qualifier("tokenStringRedisTemplate") StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public long currentVersion(Long userId) {
        if (!redisTokenEnabled) {
            return 0L;
        }

        Object value = redisTemplate.opsForHash().get(userStateKey(userId), VERSION_FIELD);
        if (value == null) {
            return 0L;
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid token version in Redis for userId={}", userId, e);
            return 0L;
        }
    }

    public boolean isAccessTokenRevoked(Long userId, Long tokenVersion, Instant issuedAt) {
        if (!redisTokenEnabled) {
            return false;
        }

        long currentVersion = currentVersion(userId);

        if (tokenVersion != null) {
            return tokenVersion < currentVersion;
        }

        Instant revokedBefore = revokedBefore(userId);
        return revokedBefore != null && issuedAt != null && issuedAt.isBefore(revokedBefore);
    }

    public long revokeAllAccessTokens(Long userId) {
        if (!redisTokenEnabled) {
            return 0L;
        }

        String key = userStateKey(userId);
        Long version = redisTemplate.opsForHash().increment(key, VERSION_FIELD, 1L);
        redisTemplate.opsForHash().put(key, REVOKED_BEFORE_FIELD, String.valueOf(Instant.now().toEpochMilli()));
        redisTemplate.expire(key, stateTtl());
        return Objects.requireNonNullElse(version, 1L);
    }

    private Instant revokedBefore(Long userId) {
        Object value = redisTemplate.opsForHash().get(userStateKey(userId), REVOKED_BEFORE_FIELD);
        if (value == null) {
            return null;
        }

        try {
            return Instant.ofEpochMilli(Long.parseLong(value.toString()));
        } catch (NumberFormatException e) {
            log.warn("Invalid revoked-before timestamp in Redis for userId={}", userId, e);
            return null;
        }
    }

    private Duration stateTtl() {
        long maxTokenLifetime = Math.max(accessExpirationMs, refreshExpirationMs);
        return Duration.ofMillis(maxTokenLifetime).plusDays(1);
    }

    private String userStateKey(Long userId) {
        return keyPrefix + ":token:user-state:" + userId;
    }
}
