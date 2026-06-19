package com.example.SinhVien5T.common.security;

import com.example.SinhVien5T.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_PUBLIC_ID = "publicId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey jwtSecretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.access-expiration}") long accessExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshExpiration
    ){
        // secretKey lưu dưới dạng BASE64 nên cần decode
        byte [] keyBytes = Decoders.BASE64.decode(secretKey);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public Claims extractAllClaims(String token){

        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token) // Ktra chữ kí (Signature)
                                          // Ktra expiryDate
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public Claims validateRefreshToken(String token) {
        Claims claims = extractAllClaims(token);

        if (!isRefreshToken(claims)) {
            throw new JwtException("Invalid token type");
        }

        return claims;
    }

    public String generateAccessJwt(User user){

        return Jwts.builder()
                .subject(user.getPublicId())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_PUBLIC_ID, user.getPublicId())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .signWith(jwtSecretKey)
                .compact();
    }

    public String generateRefreshJwt(User user, HttpServletRequest request){

        String refreshToken =  Jwts.builder()
                .subject(user.getPublicId())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_PUBLIC_ID, user.getPublicId())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .signWith(jwtSecretKey)
                .compact();

        return refreshToken;
    }

    // helper để lấy ip (để đoạn code trên gọn hơn)
    public String getIpAddress (HttpServletRequest request){

        String remoteAddress = request.getHeader("X-Forwarded-For");

        if (remoteAddress != null && !remoteAddress.isBlank()) {
            return remoteAddress.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}


