package com.example.SinhVien5T.auth.service;

import com.example.SinhVien5T.auth.exception.AuthErrorMessages;
import com.example.SinhVien5T.auth.exception.InvalidTokenException;
import com.example.SinhVien5T.common.security.JwtService;
import com.example.SinhVien5T.user.entity.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RedisTokenService {

    private static final String TOKEN_HASH_FIELD = "tokenHash";
    private static final String USER_ID_FIELD = "userId";
    private static final String PUBLIC_ID_FIELD = "publicId";
    private static final String IP_ADDRESS_FIELD = "ipAddress";
    private static final String USER_AGENT_FIELD = "userAgent";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String LAST_USED_AT_FIELD = "lastUsedAt";
    private static final String EXPIRES_AT_FIELD = "expiresAt";
    private static final Duration ROTATION_LOCK_TTL = Duration.ofSeconds(5);
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    @Value("${app.redis.key-prefix:sv5t}")
    private String keyPrefix;

    @Value("${app.auth.idle-timeout-ms:10800000}")
    private long idleTimeoutMs;

    @Value("${app.redis.token.enabled:true}")
    private boolean redisTokenEnabled;

    public RedisTokenService(
            @Qualifier("tokenStringRedisTemplate") StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public record RefreshSession(
            Long userId,
            String publicId,
            String ipAddress,
            String userAgent,
            Instant createdAt,
            Instant lastUsedAt,
            Instant expiresAt
    ) {
    }

    public void storeRefreshToken(String token, Claims claims, User user, HttpServletRequest request) {
        Duration ttl = remainingTtl(claims);
        if (ttl.isZero() || ttl.isNegative()) {
            throw new InvalidTokenException(AuthErrorMessages.SESSION_EXPIRED);
        }

        Instant now = Instant.now();
        Map<String, String> session = new HashMap<>();
        session.put(TOKEN_HASH_FIELD, sha256(token));
        session.put(USER_ID_FIELD, user.getId().toString());
        session.put(PUBLIC_ID_FIELD, user.getPublicId());
        session.put(IP_ADDRESS_FIELD, getIpAddress(request));
        session.put(USER_AGENT_FIELD, Objects.toString(request.getHeader("User-Agent"), ""));
        session.put(CREATED_AT_FIELD, now.toString());
        session.put(LAST_USED_AT_FIELD, now.toString());
        session.put(EXPIRES_AT_FIELD, claims.getExpiration().toInstant().toString());

        String refreshKey = refreshKey(claims.getId());
        redisTemplate.opsForHash().putAll(refreshKey, session);
        redisTemplate.expire(refreshKey, ttl);

        String devicesKey = userDevicesKey(user.getId());
        redisTemplate.opsForSet().add(devicesKey, claims.getId());
        redisTemplate.expire(devicesKey, ttl);
    }

    public RefreshSession validateRefreshToken(String token, Claims claims) {
        if (isRefreshTokenBlacklisted(claims.getId())) {
            throw new InvalidTokenException(AuthErrorMessages.SESSION_EXPIRED);
        }

        Map<Object, Object> session = redisTemplate.opsForHash().entries(refreshKey(claims.getId()));
        if (session.isEmpty()) {
            throw new InvalidTokenException(AuthErrorMessages.SESSION_EXPIRED);
        }

        String tokenHash = stringValue(session, TOKEN_HASH_FIELD);
        if (!sha256(token).equals(tokenHash)) {
            throw new InvalidTokenException(AuthErrorMessages.INVALID_TOKEN);
        }

        RefreshSession refreshSession = toRefreshSession(session);
        if (!Objects.equals(refreshSession.publicId(), claims.getSubject())) {
            throw new InvalidTokenException(AuthErrorMessages.INVALID_TOKEN);
        }

        if (refreshSession.lastUsedAt().plusMillis(idleTimeoutMs).isBefore(Instant.now())) {
            revokeRefreshToken(claims);
            blacklistRefreshToken(claims);
            throw new InvalidTokenException(AuthErrorMessages.SESSION_EXPIRED);
        }

        return refreshSession;
    }

    public void rotateRefreshToken(
            String oldToken,
            Claims oldClaims,
            String newToken,
            Claims newClaims,
            User user,
            HttpServletRequest request
    ) {
        String lockKey = refreshRotationLockKey(oldClaims.getId());
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, ROTATION_LOCK_TTL);

        if (!Boolean.TRUE.equals(locked)) {
            throw new InvalidTokenException(AuthErrorMessages.SESSION_EXPIRED);
        }

        try {
            validateRefreshToken(oldToken, oldClaims);
            revokeRefreshToken(oldClaims);
            blacklistRefreshToken(oldClaims);
            storeRefreshToken(newToken, newClaims, user, request);
        } finally {
            redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(lockKey), lockValue);
        }
    }

    public void revokeRefreshToken(Claims claims) {
        Long userId = claimUserId(claims);
        redisTemplate.delete(refreshKey(claims.getId()));
        if (userId != null) {
            redisTemplate.opsForSet().remove(userDevicesKey(userId), claims.getId());
        }
    }

    public void revokeAllRefreshTokens(Long userId) {
        String devicesKey = userDevicesKey(userId);
        Set<String> tokenIds = redisTemplate.opsForSet().members(devicesKey);

        if (tokenIds != null) {
            tokenIds.forEach(tokenId -> redisTemplate.delete(refreshKey(tokenId)));
        }

        redisTemplate.delete(devicesKey);
    }

    public void blacklistAccessToken(Claims claims) {
        blacklistToken(accessBlacklistKey(claims.getId()), claims);
    }

    public boolean isAccessTokenBlacklisted(String tokenId) {
        if (!redisTokenEnabled) {
            return false;
        }

        return StringUtils.hasText(tokenId)
                && Boolean.TRUE.equals(redisTemplate.hasKey(accessBlacklistKey(tokenId)));
    }

    private void blacklistRefreshToken(Claims claims) {
        blacklistToken(refreshBlacklistKey(claims.getId()), claims);
    }

    private boolean isRefreshTokenBlacklisted(String tokenId) {
        return StringUtils.hasText(tokenId)
                && Boolean.TRUE.equals(redisTemplate.hasKey(refreshBlacklistKey(tokenId)));
    }

    private void blacklistToken(String key, Claims claims) {
        Duration ttl = remainingTtl(claims);
        if (!ttl.isZero() && !ttl.isNegative()) {
            redisTemplate.opsForValue().set(key, "1", ttl);
        }
    }

    private RefreshSession toRefreshSession(Map<Object, Object> session) {
        return new RefreshSession(
                Long.valueOf(stringValue(session, USER_ID_FIELD)),
                stringValue(session, PUBLIC_ID_FIELD),
                stringValue(session, IP_ADDRESS_FIELD),
                stringValue(session, USER_AGENT_FIELD),
                Instant.parse(stringValue(session, CREATED_AT_FIELD)),
                Instant.parse(stringValue(session, LAST_USED_AT_FIELD)),
                Instant.parse(stringValue(session, EXPIRES_AT_FIELD))
        );
    }

    private String stringValue(Map<Object, Object> session, String field) {
        Object value = session.get(field);
        if (value == null) {
            throw new InvalidTokenException(AuthErrorMessages.INVALID_TOKEN);
        }
        return value.toString();
    }

    private Long claimUserId(Claims claims) {
        Object value = claims.get(JwtService.CLAIM_USER_ID);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Long.valueOf(text);
        }
        return null;
    }

    private Duration remainingTtl(Claims claims) {
        return Duration.between(Instant.now(), claims.getExpiration().toInstant());
    }

    private String refreshKey(String tokenId) {
        return keyPrefix + ":token:refresh:" + tokenId;
    }

    private String userDevicesKey(Long userId) {
        return keyPrefix + ":token:user:" + userId + ":devices";
    }

    private String accessBlacklistKey(String tokenId) {
        return keyPrefix + ":token:blacklist:access:" + tokenId;
    }

    private String refreshBlacklistKey(String tokenId) {
        return keyPrefix + ":token:blacklist:refresh:" + tokenId;
    }

    private String refreshRotationLockKey(String tokenId) {
        return keyPrefix + ":token:refresh-lock:" + tokenId;
    }

    private String sha256(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
