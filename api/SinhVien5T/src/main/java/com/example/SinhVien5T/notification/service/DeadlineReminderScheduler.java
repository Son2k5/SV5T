package com.example.SinhVien5T.notification.service;

import com.example.SinhVien5T.campaign.entity.ApplicationStatus;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.repository.ApplicationRecordRepository;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.notification.dto.NotificationEvent;
import com.example.SinhVien5T.notification.entity.NotificationType;
import com.example.SinhVien5T.notification.repository.NotificationRepository;
import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class DeadlineReminderScheduler {

    private static final String RELATED_ENTITY_CAMPAIGN = "Campaign";
    private static final int USER_PAGE_SIZE = 200;

    private final NotificationSettingService settingService;
    private final CampaignRepository campaignRepository;
    private final ApplicationRecordRepository applicationRecordRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional(readOnly = true)
    public void sendDeadlineReminders() {
        NotificationSettingService.RuntimeNotificationSetting settings = settingService.getRuntimeSettings();
        if (!settings.emailEnabled() && !settings.realtimeEnabled()) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate latestDeadline = today.plusDays(settings.reminderDaysBeforeDeadline());
        List<Campaign> campaigns = campaignRepository.findByCampaignStatusAndEndDateBetween(
                CampaignStatus.ACTIVE,
                today,
                latestDeadline
        );

        if (campaigns.isEmpty()) {
            return;
        }

        int pageIndex = 0;
        Page<User> users;
        do {
            users = userRepository.findByRoleAndIsActive(Role.USER, true, PageRequest.of(pageIndex, USER_PAGE_SIZE));
            for (Campaign campaign : campaigns) {
                sendCampaignReminders(campaign, users.getContent(), today);
            }
            pageIndex++;
        } while (users.hasNext());
    }

    private void sendCampaignReminders(Campaign campaign, List<User> users, LocalDate today) {
        if (users.isEmpty()) {
            return;
        }

        List<Long> userIds = users.stream().map(User::getId).toList();

        Set<Long> submittedUserIds = new HashSet<>(
                applicationRecordRepository.findSubmittedUserIds(
                        userIds,
                        campaign.getId(),
                        List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED)
                )
        );

        Set<Long> alreadyRemindedUserIds = new HashSet<>(
                notificationRepository.findRemindedUserIds(
                        userIds,
                        NotificationType.DEADLINE_REMINDER,
                        RELATED_ENTITY_CAMPAIGN,
                        campaign.getPublicId(),
                        today.atStartOfDay(),
                        today.atTime(LocalTime.MAX)
                )
        );

        for (User user : users) {
            if (submittedUserIds.contains(user.getId())) {
                continue;
            }
            if (alreadyRemindedUserIds.contains(user.getId())) {
                continue;
            }

            notificationService.send(new NotificationEvent(
                    user.getId(),
                    NotificationType.DEADLINE_REMINDER,
                    Map.of(
                            "studentName", displayName(user),
                            "campaignName", campaign.getName(),
                            "deadline", campaign.getEndDate() == null ? "" : campaign.getEndDate().toString()
                    ),
                    RELATED_ENTITY_CAMPAIGN,
                    campaign.getPublicId()
            ));
        }
    }

    private String displayName(User user) {
        if (user.getDetail() != null && user.getDetail().getFullName() != null && !user.getDetail().getFullName().isBlank()) {
            return user.getDetail().getFullName();
        }
        return user.getEmail();
    }
}
