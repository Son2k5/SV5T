package com.example.SinhVien5T.auth.service;

import com.example.SinhVien5T.auth.dto.request.UserLoginRequest;
import com.example.SinhVien5T.auth.dto.request.UserRegisterRequest;
import com.example.SinhVien5T.auth.exception.AuthErrorMessages;
import com.example.SinhVien5T.auth.exception.InvalidEmailDomainException;
import com.example.SinhVien5T.auth.exception.InvalidTokenException;
import com.example.SinhVien5T.common.security.CachedUserPrincipalService;
import com.example.SinhVien5T.notification.service.EmailService;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.auth.entity.RefreshToken;
import com.example.SinhVien5T.auth.entity.RegisterVerifyToken;
import com.example.SinhVien5T.auth.entity.TokenPurpose;
import com.example.SinhVien5T.user.exception.EmailExistException;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.auth.repository.RegisterVerifyTokenRepository;
import com.example.SinhVien5T.auth.repository.RefreshTokenRepository;
import com.example.SinhVien5T.user.repository.UserRepository;
import com.example.SinhVien5T.common.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RegisterVerifyTokenRepository registerVerifyTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTokenService redisTokenService;
    private final TokenStateService tokenStateService;
    private final CachedUserPrincipalService cachedUserPrincipalService;

    @Value("${app.auth.frontendUrl}")
    private String frontEndUrl;

    @Value("${app.auth.backendUrl}")
    private String backEndUrl;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Value("${app.auth.idle-timeout-ms:10800000}")
    private long idleTimeoutMs;

    @Value("${app.redis.token.enabled:true}")
    private boolean redisTokenEnabled;

    @Transactional
    public void register(UserRegisterRequest request) throws Exception {

        Optional<User> existUser = userRepository.findByEmail(request.getEmail());

        if (existUser.isPresent() && existUser.get().isVerified()) {
            throw new EmailExistException(AuthErrorMessages.EMAIL_ALREADY_REGISTERED);
        }


        if(!request.getEmail().toLowerCase().endsWith("@ms.hanu.edu.vn")){
            throw new InvalidEmailDomainException(AuthErrorMessages.INVALID_EMAIL_DOMAIN);
        }

        User user = existUser.orElseGet(() ->
                User.builder()
                        .email(request.getEmail())
                        .build()
        );

        user.setPasswordHash(passwordEncoder.encode(request.getUserPassword()));
        user.setVerified(false);

        // Lưu vào db
        userRepository.save(user);

        // Xóa tất cả token cũ trước đó
        registerVerifyTokenRepository.deleteByUserAndPurpose(user, TokenPurpose.REGISTER);

        // Tạo link verify
        String token = UUID.randomUUID().toString();

        RegisterVerifyToken registerVerifyToken = RegisterVerifyToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .purpose(TokenPurpose.REGISTER)
                .build();

        registerVerifyTokenRepository.save(registerVerifyToken);

        String verifyLink = trimTrailingSlash(backEndUrl) + "/user/auth/verify_register_token?token=" + token; // BE handle endpoint nay (ko phai viet FE)

        sendAfterCommit(() -> emailService.sendVerifyRegisterMail(verifyLink, request.getEmail()));

    }

    @Transactional
    public void verifyRegisterToken(String token, HttpServletResponse response) throws RuntimeException, IOException {

        try {
            RegisterVerifyToken registerVerifyToken = registerVerifyTokenRepository.findByTokenAndPurpose(token, TokenPurpose.REGISTER)
                    .orElseThrow(() -> new InvalidTokenException(AuthErrorMessages.INVALID_TOKEN));

            if (registerVerifyToken.getExpiryDate().isBefore(LocalDateTime.now())){

                registerVerifyTokenRepository.delete(registerVerifyToken);

                response.sendRedirect( frontEndUrl + "/login?error=token_expired");
                return;
            }

            // Link đc xác minh thành công, save isActive User rồi redirect về trong login
            User user = registerVerifyToken.getUser();
            user.setVerified(true);
            userRepository.save(user);

            registerVerifyTokenRepository.delete(registerVerifyToken);

            response.sendRedirect(frontEndUrl + "/login?verified=success");

        } catch (RuntimeException e) {
            log.error("Could not verify register token", e);
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            response.sendRedirect(frontEndUrl + "/login?error=invalid_token");
        }
    }

    public Map<String, Object> login(UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) throws MessagingException {

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                userLoginRequest.getEmail(), userLoginRequest.getUserPassword()
        );

        try{

            // 1. Xác minh user
            Authentication authentication = authenticationManager.authenticate(authRequest);

            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new BadCredentialsException(AuthErrorMessages.INVALID_CREDENTIALS));

            if (!user.isVerified()){
                throw new DisabledException(AuthErrorMessages.ACCOUNT_NOT_VERIFIED);
            }

            if (!user.isActive()){
                throw new DisabledException(AuthErrorMessages.ACCOUNT_DISABLED);
            }


            // 2. Sau khi xác thực thành công, tạo token và cho user login
            long tokenVersion = tokenStateService.currentVersion(user.getId());
            String accessToken = jwtService.generateAccessJwt(user, tokenVersion);
            String refreshToken = jwtService.generateRefreshJwt(user, request, tokenVersion);
            Claims refreshClaims = jwtService.validateRefreshToken(refreshToken);

        /*
        refreshToken sẽ được đưa vào cooke rồi gắn vào header của reponse
        accessToken đuợc đưa lên controller rồi trả về trong Body reponse
         */

            // 3. add refresh to Cookie
            addRefreshCookie(refreshToken, refreshCookieMaxAgeSeconds(), response);

            // 4. Redis in production; MySQL fallback when local Redis is disabled.
            if (redisTokenEnabled) {
                redisTokenService.storeRefreshToken(refreshToken, refreshClaims, user, request);
            } else {
                refreshTokenRepository.save(buildRefreshToken(refreshToken, user, request));
            }

            // 5. Trả accessToken về body reponse
            Map<String, Object> body = new HashMap<>();
            body.put("accessToken", accessToken);
            body.put("user", Map.of(
                    "id", user.getId(), // Nên trả về ID để Frontend dùng
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));

            return body;

        } catch (BadCredentialsException e){
            // Ném tiếp để GlobalHandler bắt (trả về 401)
            throw e;
        }
    }

    public void logOut(HttpServletRequest request, HttpServletResponse response){

        String refreshToken = getValueCookie("refreshToken", request);

        if(refreshToken != null && !refreshToken.isBlank()){
            try {
                Claims refreshClaims = jwtService.validateRefreshToken(refreshToken);
                if (redisTokenEnabled) {
                    redisTokenService.revokeRefreshToken(refreshClaims);
                } else {
                    refreshTokenRepository.deleteByToken(refreshToken);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("Ignoring invalid refresh token during logout: {}", e.getMessage());
            }
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Claims accessClaims = jwtService.validateAccessToken(authHeader.substring(7).trim());
                if (redisTokenEnabled) {
                    redisTokenService.blacklistAccessToken(accessClaims);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("Ignoring invalid access token during logout: {}", e.getMessage());
            }
        }

        // Xóa cookie trong trình duyệt
        addRefreshCookie(null, 0, response);
    }

    @Transactional
    public void missingPassWord(String email) throws MessagingException {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EmailExistException(AuthErrorMessages.ACCOUNT_NOT_FOUND)
        );

        registerVerifyTokenRepository.deleteByUserAndPurpose(user, TokenPurpose.PASSWORD_RESET);

        String token = UUID.randomUUID().toString();

        RegisterVerifyToken resetToken = RegisterVerifyToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .purpose(TokenPurpose.PASSWORD_RESET)
                .build();

        registerVerifyTokenRepository.save(resetToken);

        String resetPwLink = frontEndUrl + "/reset_password?token=" + token;

        sendAfterCommit(() -> emailService.sendResetPwMail(resetPwLink, email));
    }

    public boolean verifyResetPwToken(String token) throws RuntimeException, IOException {

        try {
            RegisterVerifyToken registerVerifyToken = registerVerifyTokenRepository.findByTokenAndPurpose(token, TokenPurpose.PASSWORD_RESET)
                    .orElseThrow(() -> new RuntimeException(AuthErrorMessages.INVALID_TOKEN));

            if (registerVerifyToken.getExpiryDate().isBefore(LocalDateTime.now())){

                registerVerifyTokenRepository.delete(registerVerifyToken);

                return false;
            }

            return true;

        } catch (RuntimeException e) {
            log.warn("Invalid password reset token: {}", e.getMessage());
            // Trường hợp lỗi khác (token rác, không tìm thấy...)
            return false;
        }
    }

    @Transactional
    public void resetPassWord(String token, String newPw) throws MessagingException {
        RegisterVerifyToken resetToken = registerVerifyTokenRepository.findByTokenAndPurpose(token, TokenPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException(AuthErrorMessages.INVALID_TOKEN));

        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            registerVerifyTokenRepository.delete(resetToken);
            throw new RuntimeException(AuthErrorMessages.INVALID_TOKEN);
        }

        registerVerifyTokenRepository.delete(resetToken);

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPw));
        userRepository.save(user);
        if (redisTokenEnabled) {
            redisTokenService.revokeAllRefreshTokens(user.getId());
            tokenStateService.revokeAllAccessTokens(user.getId());
        } else {
            refreshTokenRepository.revokeAllByUserId(user.getId());
        }
        cachedUserPrincipalService.evict(user.getId());
    }

    @Transactional
    public Map<String, Object> refreshAccessToken(HttpServletRequest request, HttpServletResponse response){

        String refreshToken = getValueCookie("refreshToken", request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw invalidRefreshToken(response);
        }

        Claims claims;
        try {
            claims = jwtService.validateRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw invalidRefreshToken(response);
        }

        if (!redisTokenEnabled) {
            return refreshAccessTokenFromDatabase(refreshToken, claims, request, response);
        }

        RedisTokenService.RefreshSession session;
        try {
            session = redisTokenService.validateRefreshToken(refreshToken, claims);
        } catch (InvalidTokenException e) {
            throw invalidRefreshToken(response);
        }

        if (tokenStateService.isAccessTokenRevoked(
                session.userId(),
                jwtService.extractTokenVersion(claims),
                claims.getIssuedAt() == null ? null : claims.getIssuedAt().toInstant()
        )) {
            redisTokenService.revokeRefreshToken(claims);
            throw invalidRefreshToken(response);
        }

        User user = userRepository.findById(session.userId())
                .orElseThrow(() -> invalidRefreshToken(response));

        if (!user.isActive()) {
            redisTokenService.revokeRefreshToken(claims);
            addRefreshCookie(null, 0, response);
            throw new DisabledException(AuthErrorMessages.ACCOUNT_DISABLED);
        }

        long tokenVersion = tokenStateService.currentVersion(user.getId());
        String newAccessToken = jwtService.generateAccessJwt(user, tokenVersion);
        String newRefreshToken = jwtService.generateRefreshJwt(user, request, tokenVersion);
        Claims newRefreshClaims = jwtService.validateRefreshToken(newRefreshToken);

        redisTokenService.rotateRefreshToken(
                refreshToken,
                claims,
                newRefreshToken,
                newRefreshClaims,
                user,
                request
        );
        addRefreshCookie(newRefreshToken, refreshCookieMaxAgeSeconds(), response);

        return Map.of("accessToken", newAccessToken);
    }




    // add RefreshToken to Cookie
    public void addRefreshCookie(String refreshToken, int maxAge, HttpServletResponse response) {

        // Tạo cookie để cho refreshToken vào
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken == null ? "" : refreshToken)
                .httpOnly(true) // Cookie sẽ không thể bị truy cập bởi JavaScript thông qua document.cookie secure
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    /*
     Hàm này để lấy giá trị của name từ cookie.

     (Ở đây sẽ dùng để lấy gtri của refreshToken)
     */
    public String getValueCookie(String name, HttpServletRequest request){

        if(request.getCookies() == null){
            return null;
        }

        return Arrays.stream(request.getCookies()) // Gtri trả về là 1 Array
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);

    }

    private String trimTrailingSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private Map<String, Object> refreshAccessTokenFromDatabase(
            String refreshToken,
            Claims claims,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        LocalDateTime now = LocalDateTime.now();

        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> token.getExpiredAt().isAfter(now))
                .filter(token -> !Boolean.TRUE.equals(token.getIsRevoked()))
                .filter(token -> token.getUser() != null && Objects.equals(token.getUser().getPublicId(), claims.getSubject()))
                .orElseThrow(() -> invalidRefreshToken(response));

        if (isRefreshTokenIdleExpired(storedRefreshToken, now)) {
            storedRefreshToken.setIsRevoked(true);
            refreshTokenRepository.save(storedRefreshToken);
            throw invalidRefreshToken(response);
        }

        User user = storedRefreshToken.getUser();
        if (!user.isActive()) {
            storedRefreshToken.setIsRevoked(true);
            refreshTokenRepository.save(storedRefreshToken);
            throw new DisabledException(AuthErrorMessages.ACCOUNT_DISABLED);
        }

        storedRefreshToken.setIsRevoked(true);
        refreshTokenRepository.save(storedRefreshToken);

        String newAccessToken = jwtService.generateAccessJwt(user, 0L);
        String newRefreshToken = jwtService.generateRefreshJwt(user, request, 0L);
        refreshTokenRepository.save(buildRefreshToken(newRefreshToken, user, request));
        addRefreshCookie(newRefreshToken, refreshCookieMaxAgeSeconds(), response);

        return Map.of("accessToken", newAccessToken);
    }

    private RefreshToken buildRefreshToken(String token, User user, HttpServletRequest request) {
        return RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .token(token)
                .user(user)
                .expiredAt(refreshTokenExpiryTime())
                .lastUsedAt(LocalDateTime.now())
                .ipAddress(jwtService.getIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();
    }

    private LocalDateTime refreshTokenExpiryTime() {
        return LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs));
    }

    private boolean isRefreshTokenIdleExpired(RefreshToken token, LocalDateTime now) {
        LocalDateTime lastUsedAt = token.getLastUsedAt();

        if (lastUsedAt == null) {
            lastUsedAt = token.getCreatedAt() == null ? now : token.getCreatedAt();
        }

        return lastUsedAt.plus(Duration.ofMillis(idleTimeoutMs)).isBefore(now);
    }

    private int refreshCookieMaxAgeSeconds() {
        long seconds = Duration.ofMillis(refreshExpirationMs).toSeconds();
        return seconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.toIntExact(seconds);
    }

    private InvalidTokenException invalidRefreshToken(HttpServletResponse response) {
        addRefreshCookie(null, 0, response);
        return new InvalidTokenException(AuthErrorMessages.SESSION_EXPIRED);
    }

    private void sendAfterCommit(EmailSender emailSender) throws MessagingException {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            emailSender.send();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    emailSender.send();
                } catch (MessagingException e) {
                    log.error("Could not send transactional email after commit", e);
                }
            }
        });
    }

    @FunctionalInterface
    private interface EmailSender {
        void send() throws MessagingException;
    }
}
