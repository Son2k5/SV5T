package com.example.SinhVien5T.notification.service;

import com.example.SinhVien5T.admin.dto.PageResponse;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.notification.dto.NotificationDto;
import com.example.SinhVien5T.notification.dto.NotificationEvent;
import com.example.SinhVien5T.notification.dto.RealtimeNotificationPayload;
import com.example.SinhVien5T.notification.entity.Notification;
import com.example.SinhVien5T.notification.entity.NotificationChannel;
import com.example.SinhVien5T.notification.entity.NotificationTemplate;
import com.example.SinhVien5T.notification.entity.NotificationType;
import com.example.SinhVien5T.notification.repository.NotificationRepository;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationSettingService settingService;
    private final NotificationTemplateService templateService;
    private final MailSenderFactory mailSenderFactory;
    private final RealtimeNotificationPublisher realtimePublisher;

    @Async("notificationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> send(NotificationEvent event) {
        if (event == null || event.recipientId() == null || event.type() == null) {
            log.warn("Ignoring invalid notification event");
            return CompletableFuture.completedFuture(null);
        }

        User recipient = userRepository.findById(event.recipientId())
                .orElse(null);
        if (recipient == null) {
            log.warn("Ignoring notification for missing recipientId={}", event.recipientId());
            return CompletableFuture.completedFuture(null);
        }

        NotificationTemplate template;
        try {
            template = templateService.findActiveTemplate(event.type());
        } catch (ResourceNotFoundException ex) {
            log.warn("Ignoring notification because template is inactive or missing: type={}", event.type());
            return CompletableFuture.completedFuture(null);
        }

        Map<String, String> payload = enrichPayload(event.payload(), recipient);
        String title = templateService.render(template.getSubject(), payload);
        String content = templateService.render(template.getBodyTemplate(), payload);

        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .content(content)
                .type(event.type())
                .relatedEntityType(blankToNull(event.relatedEntityType()))
                .relatedEntityId(blankToNull(event.relatedEntityId()))
                .build();
        Notification saved = notificationRepository.saveAndFlush(notification);

        NotificationSettingService.RuntimeNotificationSetting settings = settingService.getRuntimeSettings();
        if (shouldSendEmail(template.getChannel(), settings)) {
            sendEmail(settings, recipient, title, content);
        }
        if (shouldSendRealtime(template.getChannel(), settings)) {
            sendRealtime(recipient, saved);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public NotificationDto markAsRead(String notificationPublicId, Long userId) {
        Notification notification = notificationRepository
                .findByPublicIdAndRecipientId(notificationPublicId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay thong bao"));
        notification.setRead(true);
        return toDto(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.findByRecipientIdAndReadFalse(userId)
                .forEach(notification -> notification.setRead(true));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> getNotificationHistory(
            Long userId,
            NotificationType type,
            Pageable pageable
    ) {
        Page<Notification> page = type == null
                ? notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);

        return toPage(page, this::toDto);
    }

    public NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getPublicId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId()
        );
    }

    private void sendEmail(
            NotificationSettingService.RuntimeNotificationSetting settings,
            User recipient,
            String title,
            String content
    ) {
        if (!hasUsableEmailSettings(settings) || !StringUtils.hasText(recipient.getEmail())) {
            log.warn("Skipping email notification because SMTP or recipient email is missing");
            return;
        }

        try {
            JavaMailSender sender = mailSenderFactory.create(settings);
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(recipient.getEmail());
            helper.setSubject(title);
            helper.setText(content, true);
            if (StringUtils.hasText(settings.smtpUsername())) {
                helper.setFrom(settings.smtpUsername());
            }
            sender.send(message);
        } catch (Exception ex) {
            log.warn("Could not send notification email to userId={}", recipient.getId(), ex);
        }
    }

    private void sendRealtime(User recipient, Notification notification) {
        RealtimeNotificationPayload payload = new RealtimeNotificationPayload(
                toDto(notification),
                getUnreadCount(recipient.getId())
        );
        realtimePublisher.publish(recipient.getPublicId(), payload);
    }

    private boolean shouldSendEmail(
            NotificationChannel channel,
            NotificationSettingService.RuntimeNotificationSetting settings
    ) {
        return settings.emailEnabled()
                && (channel == NotificationChannel.EMAIL || channel == NotificationChannel.BOTH);
    }

    private boolean shouldSendRealtime(
            NotificationChannel channel,
            NotificationSettingService.RuntimeNotificationSetting settings
    ) {
        return settings.realtimeEnabled()
                && (channel == NotificationChannel.REALTIME || channel == NotificationChannel.BOTH);
    }

    private boolean hasUsableEmailSettings(NotificationSettingService.RuntimeNotificationSetting settings) {
        return StringUtils.hasText(settings.smtpHost())
                && settings.smtpPort() > 0
                && StringUtils.hasText(settings.smtpUsername())
                && StringUtils.hasText(settings.smtpPassword());
    }

    private Map<String, String> enrichPayload(Map<String, String> payload, User recipient) {
        Map<String, String> values = new HashMap<>();
        if (payload != null) {
            values.putAll(payload);
        }
        values.putIfAbsent("studentName", displayName(recipient));
        return values;
    }

    private String displayName(User user) {
        if (user.getDetail() != null && StringUtils.hasText(user.getDetail().getFullName())) {
            return user.getDetail().getFullName();
        }
        return user.getEmail();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T, R> PageResponse<R> toPage(Page<T> page, Function<T, R> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
