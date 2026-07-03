package com.example.SinhVien5T.notification.service;

import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.notification.dto.NotificationTemplateDto;
import com.example.SinhVien5T.notification.dto.NotificationTemplateRequest;
import com.example.SinhVien5T.notification.entity.NotificationChannel;
import com.example.SinhVien5T.notification.entity.NotificationTemplate;
import com.example.SinhVien5T.notification.entity.NotificationType;
import com.example.SinhVien5T.notification.repository.NotificationTemplateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}");
    private static final Set<String> ALLOWED_PLACEHOLDERS = Set.of(
            "studentName",
            "campaignName",
            "deadline",
            "reason",
            "status",
            "evidenceName",
            "systemMessage"
    );

    private final NotificationTemplateRepository templateRepository;

    @PostConstruct
    public void initializeDefaultTemplates() {
        ensureDefaultTemplates();
    }

    @Transactional
    public List<NotificationTemplateDto> listTemplates() {
        ensureDefaultTemplates();
        return templateRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public NotificationTemplateDto getTemplate(NotificationType code) {
        ensureDefaultTemplates();
        return toDto(findByCode(code));
    }

    @Transactional
    public NotificationTemplateDto updateTemplate(NotificationType code, NotificationTemplateRequest request) {
        validatePlaceholders(request.subject());
        validatePlaceholders(request.bodyTemplate());

        NotificationTemplate template = templateRepository.findByCode(code)
                .orElseGet(() -> NotificationTemplate.builder().code(code).build());
        template.setSubject(request.subject().trim());
        template.setBodyTemplate(request.bodyTemplate().trim());
        template.setChannel(request.channel());
        template.setActive(request.active() == null || request.active());

        return toDto(templateRepository.save(template));
    }

    @Transactional
    public NotificationTemplate findActiveTemplate(NotificationType code) {
        ensureDefaultTemplates();
        return templateRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Notification template is inactive or missing"));
    }

    public String render(String template, Map<String, String> payload) {
        if (!StringUtils.hasText(template)) {
            return "";
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = payload == null ? "" : payload.getOrDefault(key, "");
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    private NotificationTemplate findByCode(NotificationType code) {
        return templateRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay notification template"));
    }

    private void ensureDefaultTemplates() {
        Map<NotificationType, DefaultTemplate> defaults = defaultTemplates();
        for (Map.Entry<NotificationType, DefaultTemplate> entry : defaults.entrySet()) {
            if (templateRepository.existsByCode(entry.getKey())) {
                continue;
            }

            DefaultTemplate template = entry.getValue();
            templateRepository.save(NotificationTemplate.builder()
                    .code(entry.getKey())
                    .subject(template.subject())
                    .bodyTemplate(template.body())
                    .channel(template.channel())
                    .active(true)
                    .build());
        }
    }

    private void validatePlaceholders(String template) {
        if (!StringUtils.hasText(template)) {
            throw new IllegalArgumentException("Notification template khong duoc de trong");
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            if (!ALLOWED_PLACEHOLDERS.contains(placeholder)) {
                throw new IllegalArgumentException("Placeholder khong hop le: " + placeholder);
            }
        }
    }

    private Map<NotificationType, DefaultTemplate> defaultTemplates() {
        Map<NotificationType, DefaultTemplate> templates = new EnumMap<>(NotificationType.class);
        templates.put(
                NotificationType.SUBMISSION_RECEIVED,
                new DefaultTemplate(
                        "Da nhan ho so {{campaignName}}",
                        "Chao {{studentName}}, he thong da ghi nhan ho so cua ban cho dot {{campaignName}}.",
                        NotificationChannel.BOTH
                )
        );
        templates.put(
                NotificationType.APPROVED,
                new DefaultTemplate(
                        "Ho so {{campaignName}} da duoc duyet",
                        "Chuc mung {{studentName}}, ho so cua ban trong dot {{campaignName}} da duoc duyet.",
                        NotificationChannel.BOTH
                )
        );
        templates.put(
                NotificationType.REJECTED,
                new DefaultTemplate(
                        "Ho so {{campaignName}} bi tu choi",
                        "Chao {{studentName}}, ho so cua ban trong dot {{campaignName}} bi tu choi. Ly do: {{reason}}.",
                        NotificationChannel.BOTH
                )
        );
        templates.put(
                NotificationType.DEADLINE_REMINDER,
                new DefaultTemplate(
                        "Sap het han nop ho so {{campaignName}}",
                        "Chao {{studentName}}, dot {{campaignName}} se het han vao {{deadline}}. Vui long hoan tat ho so dung han.",
                        NotificationChannel.BOTH
                )
        );
        templates.put(
                NotificationType.SYSTEM_ALERT,
                new DefaultTemplate(
                        "Thong bao he thong",
                        "{{systemMessage}}",
                        NotificationChannel.BOTH
                )
        );
        return templates;
    }

    private NotificationTemplateDto toDto(NotificationTemplate template) {
        return new NotificationTemplateDto(
                template.getPublicId(),
                template.getCode(),
                template.getSubject(),
                template.getBodyTemplate(),
                template.getChannel(),
                template.isActive(),
                template.getUpdatedAt()
        );
    }

    private record DefaultTemplate(
            String subject,
            String body,
            NotificationChannel channel
    ) {
    }
}
