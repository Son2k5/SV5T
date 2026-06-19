package com.example.SinhVien5T.common.security;

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
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_PATH_PREFIX = "/user/auth/";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith(AUTH_PATH_PREFIX);
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
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid authorization header");
            return;
        }

        try {
            String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();
            Claims claims = jwtService.extractAllClaims(jwt);

            if (!jwtService.isAccessToken(claims)) {
                throw new JwtException("Only access tokens are accepted");
            }

            Long userId = extractRequiredLongClaim(claims, JwtService.CLAIM_USER_ID);
            String publicId = extractRequiredSubject(claims);
            String email = extractRequiredStringClaim(claims, JwtService.CLAIM_EMAIL);
            Role role = extractRequiredRole(claims);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                CustomUserDetails userDetails = new CustomUserDetails(
                        userId,
                        publicId,
                        email,
                        "",
                        role,
                        true
                );

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
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT rejected: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("Authentication failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
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
