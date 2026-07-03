package com.example.SinhVien5T.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SmtpPasswordCryptoService {

    private static final String PREFIX = "v1";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final Environment environment;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(rawPassword.getBytes(StandardCharsets.UTF_8));

            return PREFIX + ":"
                    + Base64.getEncoder().encodeToString(iv) + ":"
                    + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot encrypt SMTP password", ex);
        }
    }

    public String decrypt(String encryptedPassword) {
        if (!StringUtils.hasText(encryptedPassword)) {
            return null;
        }

        String[] parts = encryptedPassword.split(":", 3);
        if (parts.length != 3 || !PREFIX.equals(parts[0])) {
            throw new IllegalStateException("SMTP password format is not supported");
        }

        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot decrypt SMTP password", ex);
        }
    }

    private SecretKeySpec secretKey() {
        String secret = environment.getProperty("app.notification.encryption-key");
        if (!StringUtils.hasText(secret)) {
            secret = environment.getProperty("app.jwt.secret");
        }
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("Missing app.notification.encryption-key or app.jwt.secret");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, "AES");
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot derive SMTP password encryption key", ex);
        }
    }
}
