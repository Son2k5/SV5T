package com.example.SinhVien5T.common.security;

import com.example.SinhVien5T.auth.exception.AuthErrorMessages;
import com.example.SinhVien5T.auth.service.RedisTokenService;
import com.example.SinhVien5T.auth.service.TokenStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_PATH_PREFIX = "/user/auth";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final RedisTokenService redisTokenService;
    private final TokenStateService tokenStateService;
    private final CachedUserPrincipalService cachedUserPrincipalService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return servletPath.equals(AUTH_PATH_PREFIX) || servletPath.startsWith(AUTH_PATH_PREFIX + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, AuthErrorMessages.INVALID_SESSION);
            return;
        }

        try {
            String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();
            Claims claims = jwtService.validateAccessToken(jwt);

            if (redisTokenService.isAccessTokenBlacklisted(claims.getId())) {
                throw new JwtException("Access token revoked");
            }

            Long userId = extractRequiredLongClaim(claims, JwtService.CLAIM_USER_ID);
            String publicId = extractRequiredSubject(claims);
            String email = extractRequiredStringClaim(claims, JwtService.CLAIM_EMAIL);
            Role role = extractRequiredRole(claims);
            Long tokenVersion = jwtService.extractTokenVersion(claims);
            Instant issuedAt = claims.getIssuedAt() == null ? null : claims.getIssuedAt().toInstant();

            if (tokenStateService.isAccessTokenRevoked(userId, tokenVersion, issuedAt)) {
                throw new JwtException("Access token version revoked");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                CustomUserDetails userDetails = loadActiveUserDetails(userId, publicId, email, role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT expired: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, AuthErrorMessages.SESSION_EXPIRED);
            return;
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT rejected: {}", e.getMessage());
            String message = AuthErrorMessages.ACCOUNT_DISABLED.equals(e.getMessage())
                    ? AuthErrorMessages.ACCOUNT_DISABLED
                    : AuthErrorMessages.INVALID_SESSION;
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, message);
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("Authentication failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể xác thực phiên đăng nhập. Vui lòng thử lại sau.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private CustomUserDetails loadActiveUserDetails(
            Long userId,
            String publicId,
            String email,
            Role role
    ) {
        CachedUserPrincipal principal = cachedUserPrincipalService.loadByUserId(userId);

        if (!publicId.equals(principal.publicId())
                || !email.equals(principal.email())
                || role != principal.role()) {
            throw new JwtException("Token claims do not match current user");
        }

        if (!principal.active()) {
            throw new JwtException(AuthErrorMessages.ACCOUNT_DISABLED);
        }

        return principal.toUserDetails();
    }

    private Long extractRequiredLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException e) {
                throw new JwtException("Invalid " + claimName + " claim");
            }
        }

        throw new JwtException("Missing " + claimName + " claim");
    }

    private String extractRequiredStringClaim(Claims claims, String claimName) {
        String value = claims.get(claimName, String.class);

        if (!StringUtils.hasText(value)) {
            throw new JwtException("Missing " + claimName + " claim");
        }

        return value;
    }

    private String extractRequiredSubject(Claims claims) {
        String subject = claims.getSubject();

        if (!StringUtils.hasText(subject)) {
            throw new JwtException("Missing subject claim");
        }

        return subject;
    }

    private Role extractRequiredRole(Claims claims) {
        String role = extractRequiredStringClaim(claims, JwtService.CLAIM_ROLE);
        String normalizedRole = role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;

        try {
            return Role.valueOf(normalizedRole);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Invalid role claim");
        }
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   int status,
                                   String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                Map.of("success", false, "message", message)
        );
    }
}
