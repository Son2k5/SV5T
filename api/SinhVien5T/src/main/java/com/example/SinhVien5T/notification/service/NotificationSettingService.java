package com.example.SinhVien5T.notification.service;

import com.example.SinhVien5T.common.entity.AuditLog;
import com.example.SinhVien5T.common.repository.AuditLogRepository;
import com.example.SinhVien5T.notification.dto.NotificationSettingRequest;
import com.example.SinhVien5T.notification.dto.NotificationSettingResponse;
import com.example.SinhVien5T.notification.dto.SmtpTestRequest;
import com.example.SinhVien5T.notification.entity.NotificationSetting;
import com.example.SinhVien5T.notification.repository.NotificationSettingRepository;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private static final String PASSWORD_MASK = "****";
    private static final Duration SETTINGS_CACHE_TTL = Duration.ofHours(6);
    private static final Duration SMTP_TEST_RATE_LIMIT_TTL = Duration.ofMinutes(1);

    private final NotificationSettingRepository settingRepository;
    private final AuditLogRepository auditLogRepository;
    private final SmtpPasswordCryptoService passwordCryptoService;
    private final MailSenderFactory mailSenderFactory;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.redis.key-prefix:sv5t}")
    private String keyPrefix;

    public record RuntimeNotificationSetting(
            String publicId,
            String smtpHost,
            int smtpPort,
            String smtpUsername,
            String smtpPassword,
            boolean emailEnabled,
            boolean realtimeEnabled,
            int reminderDaysBeforeDeadline
    ) {
    }

    private record CachedSetting(
            String publicId,
            String smtpHost,
            Integer smtpPort,
            String smtpUsername,
            String smtpPasswordEncrypted,
            boolean emailEnabled,
            boolean realtimeEnabled,
            Integer reminderDaysBeforeDeadline,
            String updatedBy,
            LocalDateTime updatedAt
    ) {
    }

    @Transactional(readOnly = true)
    public NotificationSettingResponse getSettings() {
        return toResponse(getCachedSetting());
    }

    @Transactional(readOnly = true)
    public RuntimeNotificationSetting getRuntimeSettings() {
        CachedSetting setting = getCachedSetting();
        return new RuntimeNotificationSetting(
                setting.publicId(),
                setting.smtpHost(),
                setting.smtpPort() == null ? 587 : setting.smtpPort(),
                setting.smtpUsername(),
                passwordCryptoService.decrypt(setting.smtpPasswordEncrypted()),
                setting.emailEnabled(),
                setting.realtimeEnabled(),
                setting.reminderDaysBeforeDeadline() == null ? 3 : setting.reminderDaysBeforeDeadline()
        );
    }

    @Transactional
    public NotificationSettingResponse updateSettings(
            NotificationSettingRequest request,
            Authentication authentication
    ) {
        NotificationSetting setting = settingRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseGet(NotificationSetting::new);
        String oldValue = maskedJson(toMaskedMap(setting));

        String smtpHost = blankToNull(request.smtpHost());
        Integer smtpPort = request.smtpPort() == null ? 587 : request.smtpPort();
        String smtpUsername = blankToNull(request.smtpUsername());
        boolean emailEnabled = Boolean.TRUE.equals(request.emailEnabled());
        boolean realtimeEnabled = Boolean.TRUE.equals(request.realtimeEnabled());
        int reminderDays = request.reminderDaysBeforeDeadline() == null
                ? 3
                : request.reminderDaysBeforeDeadline();

        validateReminderDays(reminderDays);

        setting.setSmtpHost(smtpHost);
        setting.setSmtpPort(smtpPort);
        setting.setSmtpUsername(smtpUsername);
        applyPasswordUpdate(setting, request.smtpPassword());
        setting.setEmailEnabled(emailEnabled);
        setting.setRealtimeEnabled(realtimeEnabled);
        setting.setReminderDaysBeforeDeadline(reminderDays);
        setting.setUpdatedBy(actorEmail(authentication));

        validateEmailConfiguration(setting);

        NotificationSetting saved = settingRepository.save(setting);
        invalidateSettingsCache();
        cacheSetting(toCached(saved));
        auditSettingsUpdate(saved.getId(), oldValue, maskedJson(toMaskedMap(saved)), authentication);

        return toResponse(toCached(saved));
    }

    @Transactional(readOnly = true)
    public void testSmtpConnection(SmtpTestRequest request, Authentication authentication) {
        enforceSmtpTestRateLimit(authentication);

        RuntimeNotificationSetting current = getRuntimeSettings();
        RuntimeNotificationSetting testSetting = new RuntimeNotificationSetting(
                current.publicId(),
                firstText(request.smtpHost(), current.smtpHost()),
                request.smtpPort() == null ? current.smtpPort() : request.smtpPort(),
                firstText(request.smtpUsername(), current.smtpUsername()),
                passwordForTest(request.smtpPassword(), current.smtpPassword()),
                true,
                current.realtimeEnabled(),
                current.reminderDaysBeforeDeadline()
        );

        validateRuntimeEmailConfiguration(testSetting);

        try {
            JavaMailSender sender = mailSenderFactory.create(testSetting);
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(request.testEmail().trim());
            helper.setSubject("SinhVien5T SMTP test");
            helper.setText("<p>SMTP notification configuration is working.</p>", true);
            if (StringUtils.hasText(testSetting.smtpUsername())) {
                helper.setFrom(testSetting.smtpUsername());
            }
            sender.send(message);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Khong the ket noi SMTP hoac gui email test", ex);
        }
    }

    private CachedSetting getCachedSetting() {
        CachedSetting cached = readCachedSetting();
        if (cached != null) {
            return cached;
        }

        CachedSetting setting = settingRepository.findFirstByOrderByUpdatedAtDesc()
                .map(this::toCached)
                .orElseGet(this::defaultCachedSetting);
        cacheSetting(setting);
        return setting;
    }

    private CachedSetting readCachedSetting() {
        try {
            Map<Object, Object> values = stringRedisTemplate.opsForHash().entries(settingsKey());
            if (values.isEmpty()) {
                return null;
            }
            return new CachedSetting(
                    stringValue(values, "publicId"),
                    stringValue(values, "smtpHost"),
                    integerValue(values, "smtpPort"),
                    stringValue(values, "smtpUsername"),
                    stringValue(values, "smtpPasswordEncrypted"),
                    booleanValue(values, "emailEnabled"),
                    booleanValue(values, "realtimeEnabled"),
                    integerValue(values, "reminderDaysBeforeDeadline"),
                    stringValue(values, "updatedBy"),
                    localDateTimeValue(values, "updatedAt")
            );
        } catch (Exception ex) {
            log.warn("Could not read notification settings cache", ex);
            return null;
        }
    }

    private void cacheSetting(CachedSetting setting) {
        try {
            Map<String, String> values = new LinkedHashMap<>();
            put(values, "publicId", setting.publicId());
            put(values, "smtpHost", setting.smtpHost());
            put(values, "smtpPort", setting.smtpPort());
            put(values, "smtpUsername", setting.smtpUsername());
            put(values, "smtpPasswordEncrypted", setting.smtpPasswordEncrypted());
            put(values, "emailEnabled", setting.emailEnabled());
            put(values, "realtimeEnabled", setting.realtimeEnabled());
            put(values, "reminderDaysBeforeDeadline", setting.reminderDaysBeforeDeadline());
            put(values, "updatedBy", setting.updatedBy());
            put(values, "updatedAt", setting.updatedAt());
            stringRedisTemplate.opsForHash().putAll(settingsKey(), values);
            stringRedisTemplate.expire(settingsKey(), SETTINGS_CACHE_TTL);
        } catch (Exception ex) {
            log.warn("Could not write notification settings cache", ex);
        }
    }

    private void invalidateSettingsCache() {
        try {
            stringRedisTemplate.delete(settingsKey());
        } catch (Exception ex) {
            log.warn("Could not invalidate notification settings cache", ex);
        }
    }

    private void enforceSmtpTestRateLimit(Authentication authentication) {
        String key = smtpTestRateLimitKey(authentication);
        try {
            Boolean allowed = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", SMTP_TEST_RATE_LIMIT_TTL);
            if (!Boolean.TRUE.equals(allowed)) {
                throw new IllegalArgumentException("Vui long doi truoc khi test SMTP lai");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Could not check SMTP test rate limit", ex);
        }
    }

    private void applyPasswordUpdate(NotificationSetting setting, String password) {
        if (!StringUtils.hasText(password) || PASSWORD_MASK.equals(password.trim())) {
            return;
        }
        setting.setSmtpPasswordEncrypted(passwordCryptoService.encrypt(password.trim()));
    }

    private void validateEmailConfiguration(NotificationSetting setting) {
        if (!setting.isEmailEnabled()) {
            return;
        }
        validatePort(setting.getSmtpPort());
        if (!StringUtils.hasText(setting.getSmtpHost())
                || !StringUtils.hasText(setting.getSmtpUsername())
                || !StringUtils.hasText(setting.getSmtpPasswordEncrypted())) {
            throw new IllegalArgumentException("Can cau hinh day du SMTP truoc khi bat email notification");
        }
    }

    private void validateRuntimeEmailConfiguration(RuntimeNotificationSetting setting) {
        validatePort(setting.smtpPort());
        if (!StringUtils.hasText(setting.smtpHost())
                || !StringUtils.hasText(setting.smtpUsername())
                || !StringUtils.hasText(setting.smtpPassword())) {
            throw new IllegalArgumentException("Can cau hinh day du SMTP de gui email test");
        }
    }

    private void validatePort(Integer port) {
        if (port == null || port < 1 || port > 65535) {
            throw new IllegalArgumentException("SMTP port khong hop le");
        }
    }

    private void validateReminderDays(int reminderDays) {
        if (reminderDays < 0 || reminderDays > 30) {
            throw new IllegalArgumentException("So ngay nhac han phai nam trong khoang 0-30");
        }
    }

    private void auditSettingsUpdate(
            Long settingId,
            String oldValue,
            String newValue,
            Authentication authentication
    ) {
        auditLogRepository.save(AuditLog.builder()
                .entity("NotificationSetting")
                .entityId(settingId)
                .action("UPDATE_NOTIFICATION_SETTINGS")
                .actorId(actorId(authentication))
                .actorEmail(actorEmail(authentication))
                .oldValue(oldValue)
                .newValue(newValue)
                .build());
    }

    private NotificationSettingResponse toResponse(CachedSetting setting) {
        boolean passwordConfigured = StringUtils.hasText(setting.smtpPasswordEncrypted());
        return new NotificationSettingResponse(
                setting.publicId(),
                setting.smtpHost(),
                setting.smtpPort(),
                setting.smtpUsername(),
                passwordConfigured ? PASSWORD_MASK : null,
                passwordConfigured,
                setting.emailEnabled(),
                setting.realtimeEnabled(),
                setting.reminderDaysBeforeDeadline(),
                setting.updatedBy(),
                setting.updatedAt()
        );
    }

    private CachedSetting toCached(NotificationSetting setting) {
        return new CachedSetting(
                setting.getPublicId(),
                setting.getSmtpHost(),
                setting.getSmtpPort(),
                setting.getSmtpUsername(),
                setting.getSmtpPasswordEncrypted(),
                setting.isEmailEnabled(),
                setting.isRealtimeEnabled(),
                setting.getReminderDaysBeforeDeadline(),
                setting.getUpdatedBy(),
                setting.getUpdatedAt()
        );
    }

    private CachedSetting defaultCachedSetting() {
        return new CachedSetting(
                null,
                null,
                587,
                null,
                null,
                false,
                true,
                3,
                null,
                null
        );
    }

    private Map<String, Object> toMaskedMap(NotificationSetting setting) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("smtpHost", setting.getSmtpHost());
        values.put("smtpPort", setting.getSmtpPort());
        values.put("smtpUsername", setting.getSmtpUsername());
        values.put("smtpPassword", StringUtils.hasText(setting.getSmtpPasswordEncrypted()) ? PASSWORD_MASK : null);
        values.put("emailEnabled", setting.isEmailEnabled());
        values.put("realtimeEnabled", setting.isRealtimeEnabled());
        values.put("reminderDaysBeforeDeadline", setting.getReminderDaysBeforeDeadline());
        values.put("updatedBy", setting.getUpdatedBy());
        return values;
    }

    private String maskedJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return value.toString();
        }
    }

    private String passwordForTest(String requestedPassword, String currentPassword) {
        if (StringUtils.hasText(requestedPassword) && !PASSWORD_MASK.equals(requestedPassword.trim())) {
            return requestedPassword.trim();
        }
        return currentPassword;
    }

    private String firstText(String requestedValue, String currentValue) {
        return StringUtils.hasText(requestedValue) ? requestedValue.trim() : currentValue;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Long actorId(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails user
                ? user.getId()
                : null;
    }

    private String actorEmail(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails user
                ? user.getEmail()
                : null;
    }

    private String settingsKey() {
        return keyPrefix + ":notification:settings";
    }

    private String smtpTestRateLimitKey(Authentication authentication) {
        Long actorId = actorId(authentication);
        return keyPrefix + ":notification:smtp-test:" + (actorId == null ? "anonymous" : actorId);
    }

    private void put(Map<String, String> values, String key, Object value) {
        if (value != null) {
            values.put(key, value.toString());
        }
    }

    private String stringValue(Map<Object, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }

    private Integer integerValue(Map<Object, Object> values, String key) {
        String value = stringValue(values, key);
        return StringUtils.hasText(value) ? Integer.valueOf(value) : null;
    }

    private boolean booleanValue(Map<Object, Object> values, String key) {
        return Boolean.parseBoolean(stringValue(values, key));
    }

    private LocalDateTime localDateTimeValue(Map<Object, Object> values, String key) {
        String value = stringValue(values, key);
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }
}
