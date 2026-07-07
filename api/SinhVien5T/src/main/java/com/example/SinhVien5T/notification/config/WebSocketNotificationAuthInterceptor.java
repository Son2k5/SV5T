package com.example.SinhVien5T.notification.config;

import com.example.SinhVien5T.auth.service.RedisTokenService;
import com.example.SinhVien5T.auth.service.TokenStateService;
import com.example.SinhVien5T.chat.repository.ChatRoomRepository;
import com.example.SinhVien5T.common.security.CachedUserPrincipal;
import com.example.SinhVien5T.common.security.CachedUserPrincipalService;
import com.example.SinhVien5T.common.security.JwtService;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String NOTIFICATION_TOPIC_PREFIX = "/topic/notifications/";
    private static final String CHAT_ADMIN_TOPIC = "/topic/chat/admin";
    private static final String CHAT_ROOM_TOPIC_PREFIX = "/topic/chat/rooms/";

    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final TokenStateService tokenStateService;
    private final CachedUserPrincipalService cachedUserPrincipalService;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(authenticate(accessor));
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            assertOwnNotificationTopic(accessor);
            assertAllowedChatTopic(accessor);
        }

        return message;
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("Missing websocket authorization");
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            Claims claims = jwtService.validateAccessToken(token);
            if (redisTokenService.isAccessTokenBlacklisted(claims.getId())) {
                throw new JwtException("Access token revoked");
            }

            Long userId = requiredLongClaim(claims, JwtService.CLAIM_USER_ID);
            String publicId = requiredSubject(claims);
            String email = requiredStringClaim(claims, JwtService.CLAIM_EMAIL);
            Role role = requiredRole(claims);
            Long tokenVersion = jwtService.extractTokenVersion(claims);
            Instant issuedAt = claims.getIssuedAt() == null ? null : claims.getIssuedAt().toInstant();

            if (tokenStateService.isAccessTokenRevoked(userId, tokenVersion, issuedAt)) {
                throw new JwtException("Access token version revoked");
            }

            CachedUserPrincipal principal = cachedUserPrincipalService.loadByUserId(userId);
            if (!publicId.equals(principal.publicId()) || !email.equals(principal.email()) || role != principal.role()) {
                throw new JwtException("Token claims do not match current user");
            }
            if (!principal.active()) {
                throw new JwtException("User is disabled");
            }

            CustomUserDetails userDetails = principal.toUserDetails();
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new MessagingException("Invalid websocket authorization", ex);
        }
    }

    private void assertOwnNotificationTopic(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination) || !destination.startsWith(NOTIFICATION_TOPIC_PREFIX)) {
            return;
        }

        if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication)
                || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new MessagingException("Missing websocket user");
        }

        String expectedDestination = NOTIFICATION_TOPIC_PREFIX + user.getPublicId();
        if (!expectedDestination.equals(destination)) {
            throw new MessagingException("Cannot subscribe to another user's notifications");
        }
    }

    private void assertAllowedChatTopic(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination)
                || (!destination.equals(CHAT_ADMIN_TOPIC) && !destination.startsWith(CHAT_ROOM_TOPIC_PREFIX))) {
            return;
        }

        CustomUserDetails user = websocketUser(accessor);
        if (destination.equals(CHAT_ADMIN_TOPIC)) {
            if (user.getRole() != Role.ADMIN) {
                throw new MessagingException("Only admins can subscribe to the chat inbox");
            }
            return;
        }

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        String roomPublicId = destination.substring(CHAT_ROOM_TOPIC_PREFIX.length());
        if (!StringUtils.hasText(roomPublicId)
                || !chatRoomRepository.existsByPublicIdAndUserId(roomPublicId, user.getId())) {
            throw new MessagingException("Cannot subscribe to another user's chat room");
        }
    }

    private CustomUserDetails websocketUser(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication)
                || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new MessagingException("Missing websocket user");
        }
        return user;
    }

    private Long requiredLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Long.valueOf(text);
        }
        throw new JwtException("Missing " + claimName + " claim");
    }

    private String requiredStringClaim(Claims claims, String claimName) {
        String value = claims.get(claimName, String.class);
        if (!StringUtils.hasText(value)) {
            throw new JwtException("Missing " + claimName + " claim");
        }
        return value;
    }

    private String requiredSubject(Claims claims) {
        if (!StringUtils.hasText(claims.getSubject())) {
            throw new JwtException("Missing subject claim");
        }
        return claims.getSubject();
    }

    private Role requiredRole(Claims claims) {
        String role = requiredStringClaim(claims, JwtService.CLAIM_ROLE);
        String normalized = role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
        return Role.valueOf(normalized);
    }
}
