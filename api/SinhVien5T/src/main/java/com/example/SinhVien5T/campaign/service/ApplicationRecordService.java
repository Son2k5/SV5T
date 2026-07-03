package com.example.SinhVien5T.campaign.service;

import com.example.SinhVien5T.campaign.dto.ApplicationRecordResponse;
import com.example.SinhVien5T.campaign.dto.CreateApplicationRecordRequest;
import com.example.SinhVien5T.campaign.dto.SaveEvidenceRequest;
import com.example.SinhVien5T.campaign.entity.ApplicationRecord;
import com.example.SinhVien5T.campaign.entity.ApplicationStatus;
import com.example.SinhVien5T.campaign.entity.Level;
import com.example.SinhVien5T.campaign.exception.InvalidApplicationRecordStateException;
import com.example.SinhVien5T.campaign.repository.ApplicationRecordRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Evidence;
import com.example.SinhVien5T.campaign.entity.EvidenceStatus;
import com.example.SinhVien5T.campaign.entity.EvidenceType;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.repository.CriteriaRepository;
import com.example.SinhVien5T.campaign.repository.EvidenceRepository;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.common.config.CacheConfig;
import com.example.SinhVien5T.common.service.AssetCleanupScheduler;
import com.example.SinhVien5T.common.service.CloudinaryStorageService;
import com.example.SinhVien5T.common.service.StoredAsset;
import com.example.SinhVien5T.notification.dto.NotificationEvent;
import com.example.SinhVien5T.notification.entity.NotificationType;
import com.example.SinhVien5T.notification.service.NotificationEventPublisher;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationRecordService {

    private static final int MAX_EVIDENCE_URL_LENGTH = 1000;

    private final ApplicationRecordRepository applicationRecordRepository;
    private final CampaignRepository campaignRepository;
    private final CriteriaRepository criteriaRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final AssetCleanupScheduler assetCleanupScheduler;
    private final NotificationEventPublisher notificationEventPublisher;

    @Caching(
            put = @CachePut(
                    cacheNames = CacheConfig.APPLICATION_RECORD_MINE,
                    key = "#request.campaignPublicId + ':' + (#request.isGroup != null ? #request.isGroup : false) + ':' + (#request.level != null ? #request.level.trim().toUpperCase() : 'UNIVERSITY') + ':' + #authentication.principal.id"
            ),
            evict = @CacheEvict(
                    cacheNames = CacheConfig.CAMPAIGN_CRITERIA_USER,
                    key = "#request.campaignPublicId + ':' + (#request.isGroup != null ? #request.isGroup : false) + ':' + (#request.level != null ? #request.level.trim().toUpperCase() : 'UNIVERSITY') + ':' + #authentication.principal.id"
            )
    )
    @Transactional
    public ApplicationRecordResponse create(
            CreateApplicationRecordRequest request,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Campaign campaign = campaignRepository.findByPublicId(request.getCampaignPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));

        Level recordLevel = null;
        if (request.getLevel() != null && !request.getLevel().isBlank()) {
            try {
                recordLevel = Level.valueOf(request.getLevel().trim().toUpperCase());
            } catch (Exception e) {
                // ignore, defaults below
            }
        }
        if (recordLevel == null) {
            recordLevel = campaign.getLevel().getDefaultLevel();
        }

        Boolean isGroup = request.getIsGroup() != null && request.getIsGroup();
        ApplicationRecord existingRecord = applicationRecordRepository
                .findByUserIdAndCampaignIdAndIsGroupAndLevel(
                        currentUser.getId(),
                        campaign.getId(),
                        isGroup,
                        recordLevel
                )
                .orElse(null);
        if (existingRecord != null) {
            return toResponse(existingRecord);
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        ApplicationRecord record = ApplicationRecord.builder()
                .user(user)
                .campaign(campaign)
                .status(ApplicationStatus.DRAFT)
                .note(request.getNote())
                .isGroup(isGroup)
                .level(recordLevel)
                .build();

        return toResponse(applicationRecordRepository.save(record));
    }

    @Cacheable(
            cacheNames = CacheConfig.APPLICATION_RECORD_MINE,
            key = "#campaignPublicId + ':' + (#isGroup != null ? #isGroup : false) + ':' + (#level != null ? #level.name() : 'UNIVERSITY') + ':' + #authentication.principal.id",
            sync = true
    )
    @Transactional(readOnly = true)
    public ApplicationRecordResponse getMine(
            String campaignPublicId,
            Boolean isGroup,
            Level level,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Level targetLevel = level;
        if (targetLevel == null) {
            Campaign campaign = campaignRepository.findByPublicId(campaignPublicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));
            targetLevel = campaign.getLevel().getDefaultLevel();
        }

        return applicationRecordRepository
                .findByUserIdAndCampaignPublicIdAndIsGroupAndLevel(currentUser.getId(), campaignPublicId, isGroup != null ? isGroup : false, targetLevel)
                .map(this::toResponse)
                .orElse(null);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAMPAIGN_CRITERIA_USER, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.APPLICATION_RECORD_MINE, allEntries = true)
    })
    @Transactional
    public void saveEvidence(
            String campaignPublicId,
            SaveEvidenceRequest request,
            Boolean isGroup,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Campaign campaign = campaignRepository.findByPublicId(campaignPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));

        Criteria criteria = criteriaRepository
                .findByPublicIdAndStandardCampaignId(request.getCriteriaPublicId(), campaign.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tiêu chí không thuộc đợt xét chọn này"));

        Level targetLevel = criteria.getStandard().getLevel();
        if (targetLevel == null) {
            targetLevel = campaign.getLevel().getDefaultLevel();
        }

        ApplicationRecord record = applicationRecordRepository
                .findByUserIdAndCampaignIdAndIsGroupAndLevel(currentUser.getId(), campaign.getId(), isGroup != null ? isGroup : false, targetLevel)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký hồ sơ cho cấp này trong đợt xét chọn"));

        if (record.getStatus() != ApplicationStatus.DRAFT) {
            throw new InvalidApplicationRecordStateException("Hồ sơ đã nộp, không thể sửa minh chứng");
        }

        Evidence evidence = evidenceRepository
                .findByApplicationRecordIdAndCriteriaId(record.getId(), criteria.getId())
                .orElseGet(Evidence::new);

        evidence.setApplicationRecord(record);
        evidence.setCriteria(criteria);
        String oldPublicId = evidence.getEvidencePublicId();
        String oldResourceType = evidence.getEvidenceResourceType();
        String evidenceUrl = request.getEvidenceUrl().trim();
        if (!cloudinaryStorageService.isImageDataUrl(evidenceUrl)) {
            validateExpectedEvidenceType(criteria, EvidenceType.LINK);
            validateEvidenceUrl(evidenceUrl);
        }
        if (cloudinaryStorageService.isImageDataUrl(evidenceUrl)) {
            validateExpectedEvidenceType(criteria, EvidenceType.IMAGE);
            StoredAsset storedAsset = cloudinaryStorageService.uploadEvidenceDataUrl(
                    evidenceUrl,
                    campaign.getPublicId(),
                    currentUser.getPublicId(),
                    criteria.getId()
            );
            assetCleanupScheduler.deleteAfterRollback(storedAsset.publicId(), storedAsset.resourceType());
            applyEvidenceAsset(evidence, storedAsset);
        } else if (evidenceUrl.length() > 1000) {
            throw new IllegalArgumentException("Link minh chứng không được vượt quá 1000 ký tự");
        } else {
            evidence.setEvidenceUrl(evidenceUrl);
            evidence.setEvidencePublicId(null);
            evidence.setEvidenceResourceType(null);
            evidence.setEvidenceFormat(null);
            evidence.setEvidenceOriginalFilename(null);
        }

        evidence.setEvidenceStatus(EvidenceStatus.PENDING);
        evidence.setReviewerComment(null);

        evidenceRepository.saveAndFlush(evidence);
        assetCleanupScheduler.deleteAfterCommit(oldPublicId, oldResourceType);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAMPAIGN_CRITERIA_USER, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.APPLICATION_RECORD_MINE, allEntries = true)
    })
    @Transactional
    public void saveEvidenceFile(
            String campaignPublicId,
            String criteriaPublicId,
            MultipartFile file,
            Boolean isGroup,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Campaign campaign = campaignRepository.findByPublicId(campaignPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));

        Criteria criteria = criteriaRepository
                .findByPublicIdAndStandardCampaignId(criteriaPublicId, campaign.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tiêu chí không thuộc đợt xét chọn này"));

        Level targetLevel = criteria.getStandard().getLevel();
        if (targetLevel == null) {
            targetLevel = campaign.getLevel().getDefaultLevel();
        }

        ApplicationRecord record = applicationRecordRepository
                .findByUserIdAndCampaignIdAndIsGroupAndLevel(currentUser.getId(), campaign.getId(), isGroup != null ? isGroup : false, targetLevel)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký hồ sơ cho cấp này trong đợt xét chọn"));

        if (record.getStatus() != ApplicationStatus.DRAFT) {
            throw new InvalidApplicationRecordStateException("Hồ sơ đã nộp, không thể sửa minh chứng");
        }

        validateExpectedEvidenceType(criteria, evidenceTypeForFile(file));

        Evidence evidence = evidenceRepository
                .findByApplicationRecordIdAndCriteriaId(record.getId(), criteria.getId())
                .orElseGet(Evidence::new);

        String oldPublicId = evidence.getEvidencePublicId();
        String oldResourceType = evidence.getEvidenceResourceType();

        StoredAsset storedAsset = cloudinaryStorageService.uploadEvidenceFile(
                file,
                campaign.getPublicId(),
                currentUser.getPublicId(),
                criteria.getId()
        );
        assetCleanupScheduler.deleteAfterRollback(storedAsset.publicId(), storedAsset.resourceType());

        evidence.setApplicationRecord(record);
        evidence.setCriteria(criteria);
        applyEvidenceAsset(evidence, storedAsset);
        evidence.setEvidenceStatus(EvidenceStatus.PENDING);
        evidence.setReviewerComment(null);

        evidenceRepository.saveAndFlush(evidence);
        assetCleanupScheduler.deleteAfterCommit(oldPublicId, oldResourceType);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAMPAIGN_CRITERIA_USER, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.APPLICATION_RECORD_MINE, allEntries = true)
    })
    @Transactional
    public ApplicationRecordResponse submit(
            String campaignPublicId,
            Boolean isGroup,
            Level level,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Level targetLevel = level;
        if (targetLevel == null) {
            Campaign campaign = campaignRepository.findByPublicId(campaignPublicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));
            targetLevel = campaign.getLevel().getDefaultLevel();
        }

        ApplicationRecord record = applicationRecordRepository
                .findByUserIdAndCampaignPublicIdAndIsGroupAndLevel(currentUser.getId(), campaignPublicId, isGroup != null ? isGroup : false, targetLevel)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));

        if (record.getStatus() != ApplicationStatus.DRAFT) {
            throw new InvalidApplicationRecordStateException("Chỉ hồ sơ nháp mới được nộp");
        }

        validateSubmission(record);
        record.setStatus(ApplicationStatus.SUBMITTED);

        ApplicationRecord saved = applicationRecordRepository.save(record);
        publishSubmissionNotification(saved);

        return toResponse(saved);
    }

    private void publishSubmissionNotification(ApplicationRecord record) {
        notificationEventPublisher.publishAfterCommit(new NotificationEvent(
                record.getUser().getId(),
                NotificationType.SUBMISSION_RECEIVED,
                Map.of(
                        "studentName", displayName(record.getUser()),
                        "campaignName", record.getCampaign().getName(),
                        "status", record.getStatus().name()
                ),
                "ApplicationRecord",
                record.getPublicId()
        ));
    }

    private CustomUserDetails getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new InvalidApplicationRecordStateException("Người dùng chưa đăng nhập");
        }

        return user;
    }

    private ApplicationRecordResponse toResponse(ApplicationRecord record) {
        return ApplicationRecordResponse.builder()
                .publicId(record.getPublicId())
                .campaignPublicId(record.getCampaign().getPublicId())
                .campaignName(record.getCampaign().getName())
                .level(record.getLevel() != null ? record.getLevel().name() : record.getCampaign().getLevel().name())
                .status(record.getStatus().name())
                .note(record.getNote())
                .isGroup(record.getIsGroup())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private String displayName(User user) {
        if (user.getDetail() != null
                && user.getDetail().getFullName() != null
                && !user.getDetail().getFullName().isBlank()) {
            return user.getDetail().getFullName();
        }
        return user.getEmail();
    }

    private void applyEvidenceAsset(Evidence evidence, StoredAsset storedAsset) {
        evidence.setEvidenceUrl(storedAsset.url());
        evidence.setEvidencePublicId(storedAsset.publicId());
        evidence.setEvidenceResourceType(storedAsset.resourceType());
        evidence.setEvidenceFormat(storedAsset.format());
        evidence.setEvidenceOriginalFilename(storedAsset.originalFilename());
    }

    private void validateSubmission(ApplicationRecord record) {
        List<Criteria> criteriaList = criteriaRepository
                .findAllByCampaignIdAndIsGroupWithStandardAndParent(
                        record.getCampaign().getId(),
                        record.getIsGroup() != null ? record.getIsGroup() : false
                );

        if (record.getCampaign().getLevel().isMultiLevel()) {
            final Level recordLevel = record.getLevel();
            criteriaList = criteriaList.stream()
                    .filter(c -> c.getStandard().getLevel() == recordLevel)
                    .toList();
        }
        Map<Long, Evidence> evidenceByCriteriaId = evidenceRepository.findByApplicationRecordId(record.getId())
                .stream()
                .collect(Collectors.toMap(
                        evidence -> evidence.getCriteria().getId(),
                        evidence -> evidence,
                        (left, right) -> left
                ));
        Map<Long, List<Criteria>> childrenByParentId = criteriaList.stream()
                .filter(criteria -> criteria.getParent() != null)
                .collect(Collectors.groupingBy(criteria -> criteria.getParent().getId()));
        Map<Long, Boolean> satisfiedMemo = new HashMap<>();

        for (Criteria criteria : criteriaList) {
            if (Boolean.TRUE.equals(criteria.getIsMandatory())
                    && requiresEvidence(criteria)
                    && !hasSubmittedEvidence(criteria, evidenceByCriteriaId)) {
                throw new InvalidApplicationRecordStateException(
                        "Tiêu chí bắt buộc chưa có minh chứng: " + criteria.getName()
                );
            }

            int requiredChildrenCount = requiredChildrenCount(criteria);
            if (requiredChildrenCount > 0) {
                long satisfiedChildren = childrenByParentId.getOrDefault(criteria.getId(), List.of())
                        .stream()
                        .filter(child -> isCriteriaSatisfied(
                                child,
                                childrenByParentId,
                                evidenceByCriteriaId,
                                satisfiedMemo
                        ))
                        .count();
                if (satisfiedChildren < requiredChildrenCount) {
                    throw new InvalidApplicationRecordStateException(
                            "Tiêu chí chưa đủ minh chứng con: " + criteria.getName()
                    );
                }
            }
        }
    }

    private boolean isCriteriaSatisfied(
            Criteria criteria,
            Map<Long, List<Criteria>> childrenByParentId,
            Map<Long, Evidence> evidenceByCriteriaId,
            Map<Long, Boolean> satisfiedMemo
    ) {
        Boolean cached = satisfiedMemo.get(criteria.getId());
        if (cached != null) {
            return cached;
        }

        boolean hasEvidence = hasSubmittedEvidence(criteria, evidenceByCriteriaId);
        int requiredChildrenCount = requiredChildrenCount(criteria);
        boolean hasEnoughChildren = true;
        if (requiredChildrenCount > 0) {
            long satisfiedChildren = childrenByParentId.getOrDefault(criteria.getId(), List.of())
                    .stream()
                    .filter(child -> isCriteriaSatisfied(
                            child,
                            childrenByParentId,
                            evidenceByCriteriaId,
                            satisfiedMemo
                    ))
                    .count();
            hasEnoughChildren = satisfiedChildren >= requiredChildrenCount;
        }

        boolean satisfied = requiresEvidence(criteria)
                ? hasEvidence && hasEnoughChildren
                : requiredChildrenCount > 0 && hasEnoughChildren;
        satisfiedMemo.put(criteria.getId(), satisfied);
        return satisfied;
    }

    private boolean hasSubmittedEvidence(Criteria criteria, Map<Long, Evidence> evidenceByCriteriaId) {
        Evidence evidence = evidenceByCriteriaId.get(criteria.getId());
        return evidence != null && evidence.getEvidenceStatus() != EvidenceStatus.REJECTED;
    }

    private boolean requiresEvidence(Criteria criteria) {
        EvidenceType evidenceType = criteria.getEvidenceType();
        return evidenceType != null && evidenceType != EvidenceType.NONE;
    }

    private int requiredChildrenCount(Criteria criteria) {
        return criteria.getRequiredChildrenCount() == null ? 0 : criteria.getRequiredChildrenCount();
    }

    private void validateExpectedEvidenceType(Criteria criteria, EvidenceType actualType) {
        EvidenceType expectedType = criteria.getEvidenceType();
        if (expectedType == null || expectedType == EvidenceType.FILE) {
            return;
        }
        if (expectedType == EvidenceType.NONE) {
            throw new IllegalArgumentException("Tiêu chí này không yêu cầu minh chứng");
        }
        if (expectedType != actualType) {
            throw new IllegalArgumentException("Loại minh chứng không phù hợp với tiêu chí");
        }
    }

    private void validateEvidenceUrl(String evidenceUrl) {
        if (evidenceUrl.length() > MAX_EVIDENCE_URL_LENGTH) {
            throw new IllegalArgumentException("Link minh chứng không được vượt quá 1000 ký tự");
        }

        try {
            URI uri = new URI(evidenceUrl);
            String scheme = uri.getScheme() == null
                    ? ""
                    : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!("http".equals(scheme) || "https".equals(scheme)) || uri.getHost() == null) {
                throw new IllegalArgumentException("Link minh chứng không hợp lệ");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Link minh chứng không hợp lệ");
        }
    }

    private EvidenceType evidenceTypeForFile(MultipartFile file) {
        String filename = file == null ? "" : file.getOriginalFilename();
        String extension = "";
        int dotIndex = filename == null ? -1 : filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        }

        return switch (extension) {
            case "png", "jpg", "jpeg", "webp" -> EvidenceType.IMAGE;
            case "pdf" -> EvidenceType.PDF;
            default -> EvidenceType.FILE;
        };
    }
}
