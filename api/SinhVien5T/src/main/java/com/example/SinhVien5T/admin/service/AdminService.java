package com.example.SinhVien5T.admin.service;

import com.example.SinhVien5T.admin.dto.AdminUserCreateRequest;
import com.example.SinhVien5T.admin.dto.AdminUserUpdateRequest;
import com.example.SinhVien5T.admin.dto.CampaignRequest;
import com.example.SinhVien5T.admin.dto.CampaignResponse;
import com.example.SinhVien5T.admin.dto.CampaignStatsResponse;
import com.example.SinhVien5T.admin.dto.CampaignStatusRequest;
import com.example.SinhVien5T.admin.dto.CriteriaRequest;
import com.example.SinhVien5T.admin.dto.CriteriaRequirementRequest;
import com.example.SinhVien5T.admin.dto.CriteriaResponse;
import com.example.SinhVien5T.admin.dto.DashboardResponse;
import com.example.SinhVien5T.admin.dto.EvidenceResponse;
import com.example.SinhVien5T.admin.dto.IdsRequest;
import com.example.SinhVien5T.admin.dto.PageResponse;
import com.example.SinhVien5T.admin.dto.RejectRequest;
import com.example.SinhVien5T.admin.dto.ResultDetailResponse;
import com.example.SinhVien5T.admin.dto.ResultResponse;
import com.example.SinhVien5T.admin.dto.SettingRequest;
import com.example.SinhVien5T.admin.dto.SettingResponse;
import com.example.SinhVien5T.admin.dto.StandardRequest;
import com.example.SinhVien5T.admin.dto.StandardResponse;
import com.example.SinhVien5T.admin.dto.UpdatePermissionRequest;
import com.example.SinhVien5T.admin.dto.UpdateRoleRequest;
import com.example.SinhVien5T.admin.dto.UpdateStatusRequest;
import com.example.SinhVien5T.admin.dto.UserAdminResponse;
import com.example.SinhVien5T.admin.repository.AdminApplicationRecordRepository;
import com.example.SinhVien5T.admin.repository.AdminAuditLogRepository;
import com.example.SinhVien5T.admin.repository.AdminCampaignRepository;
import com.example.SinhVien5T.admin.repository.AdminCriteriaRepository;
import com.example.SinhVien5T.admin.repository.AdminEvidenceRepository;
import com.example.SinhVien5T.admin.repository.AdminPermissionRepository;
import com.example.SinhVien5T.admin.repository.AdminStandardRepository;
import com.example.SinhVien5T.admin.repository.AdminSystemSettingRepository;
import com.example.SinhVien5T.admin.repository.AdminUserPermissionRepository;
import com.example.SinhVien5T.admin.repository.AdminUserRepository;
import com.example.SinhVien5T.campaign.dto.SaveEvidenceRequest;
import com.example.SinhVien5T.campaign.dto.StandardDTO;
import com.example.SinhVien5T.campaign.dto.CriteriaDTO;
import com.example.SinhVien5T.campaign.entity.ApplicationRecord;
import com.example.SinhVien5T.campaign.entity.ApplicationStatus;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Evidence;
import com.example.SinhVien5T.campaign.entity.EvidenceStatus;
import com.example.SinhVien5T.campaign.entity.EvidenceType;
import com.example.SinhVien5T.campaign.entity.Level;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.common.config.CacheConfig;
import com.example.SinhVien5T.common.entity.AuditLog;
import com.example.SinhVien5T.common.entity.Permission;
import com.example.SinhVien5T.common.entity.SystemSetting;
import com.example.SinhVien5T.common.entity.UserPermission;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.common.security.CachedUserPrincipalService;
import com.example.SinhVien5T.notification.dto.NotificationEvent;
import com.example.SinhVien5T.notification.entity.NotificationType;
import com.example.SinhVien5T.notification.service.NotificationEventPublisher;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.admin.dto.CriteriaTemplateRequest;
import com.example.SinhVien5T.admin.dto.CriteriaTemplateResponse;
import com.example.SinhVien5T.campaign.entity.CriteriaTemplate;
import com.example.SinhVien5T.campaign.repository.CriteriaTemplateRepository;
import com.example.SinhVien5T.user.entity.Role;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.entity.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminUserRepository userRepository;
    private final AdminCampaignRepository campaignRepository;
    private final AdminStandardRepository standardRepository;
    private final AdminCriteriaRepository criteriaRepository;
    private final AdminEvidenceRepository evidenceRepository;
    private final AdminApplicationRecordRepository applicationRecordRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final AdminSystemSettingRepository settingRepository;
    private final AdminPermissionRepository permissionRepository;
    private final AdminUserPermissionRepository userPermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CachedUserPrincipalService cachedUserPrincipalService;
    private final CacheManager cacheManager;
    private final NotificationEventPublisher notificationEventPublisher;
    private final CriteriaTemplateRepository criteriaTemplateRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> getUsers(
            int page,
            int size,
            String keyword,
            Role role,
            Boolean active
    ) {
        Page<User> users = userRepository.search(
                like(keyword),
                role,
                active,
                PageRequest.of(page, size)
        );

        return toPage(users, this::toUser);
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUser(String userPublicId) {
        return toUser(findUser(userPublicId));
    }

    @Transactional
    public UserAdminResponse createUser(AdminUserCreateRequest request, Authentication authentication) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .createdBy(actorEmail(authentication))
                .build();
        user.setActive(true);
        user.setVerified(true);
        applyCreateDetail(user, request);

        User saved = userRepository.save(user);
        audit("User", saved.getId(), "CREATE", null, saved.getEmail(), authentication);
        cachedUserPrincipalService.evict(saved.getId());

        return toUser(saved);
    }

    @Transactional
    public UserAdminResponse updateUser(
            String userPublicId,
            AdminUserUpdateRequest request,
            Authentication authentication
    ) {
        User user = findUser(userPublicId);
        String oldValue = user.getEmail() + "|" + user.getRole() + "|" + user.isActive();

        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            String email = request.email().trim().toLowerCase(Locale.ROOT);
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            user.setEmail(email);
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        user.setUpdatedBy(actorEmail(authentication));
        applyUpdateDetail(user, request);

        audit(
                "User",
                user.getId(),
                "UPDATE",
                oldValue,
                user.getEmail() + "|" + user.getRole() + "|" + user.isActive(),
                authentication
        );
        cachedUserPrincipalService.evict(user.getId());

        return toUser(user);
    }

    @Transactional
    public UserAdminResponse changeRole(
            String userPublicId,
            UpdateRoleRequest request,
            Authentication authentication
    ) {
        User user = findUser(userPublicId);
        Role oldRole = user.getRole();
        user.setRole(request.role());

        audit("User", user.getId(), "CHANGE_ROLE", oldRole.name(), request.role().name(), authentication);
        cachedUserPrincipalService.evict(user.getId());

        return toUser(user);
    }

    @Transactional
    public UserAdminResponse changeStatus(
            String userPublicId,
            UpdateStatusRequest request,
            Authentication authentication
    ) {
        User user = findUser(userPublicId);
        boolean oldActive = user.isActive();
        user.setActive(request.active());

        audit(
                "User",
                user.getId(),
                "CHANGE_STATUS",
                String.valueOf(oldActive),
                String.valueOf(request.active()),
                authentication
        );
        cachedUserPrincipalService.evict(user.getId());

        return toUser(user);
    }

    @Transactional
    public void deleteUser(String userPublicId, Authentication authentication) {
        User user = findUser(userPublicId);
        user.setActive(false);

        audit("User", user.getId(), "DELETE", "active=true", "active=false", authentication);
        cachedUserPrincipalService.evict(user.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLog> getUserHistory(String userPublicId, int page, int size) {
        User user = findUser(userPublicId);
        return toPage(
                auditLogRepository.findByEntityAndEntityId(
                        "User",
                        user.getId(),
                        PageRequest.of(page, size)
                ),
                Function.identity()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> getCampaigns(
            int page,
            int size,
            CampaignStatus status,
            Level level,
            Boolean isGroup
    ) {
        return toPage(
                campaignRepository.search(status, level, isGroup, PageRequest.of(page, size)),
                this::toCampaign
        );
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaign(String campaignPublicId) {
        return toCampaign(findCampaign(campaignPublicId));
    }

    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request, Authentication authentication) {
        Campaign campaign = Campaign.builder()
                .name(request.name())
                .description(request.description())
                .academicYear(request.academicYear())
                .level(request.level())
                .campaignStatus(request.status() == null ? CampaignStatus.DRAFT : request.status())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .isGroup(request.isGroup() != null ? com.example.SinhVien5T.campaign.entity.CampaignType.valueOf(request.isGroup().trim().toUpperCase()) : com.example.SinhVien5T.campaign.entity.CampaignType.INDIVIDUAL)
                .build();

        Campaign saved = campaignRepository.save(campaign);
        clearCampaignCaches();
        audit("Campaign", saved.getId(), "CREATE", null, saved.getName(), authentication);

        return toCampaign(saved);
    }

    @Transactional
    public CampaignResponse updateCampaign(
            String campaignPublicId,
            CampaignRequest request,
            Authentication authentication
    ) {
        Campaign campaign = findCampaign(campaignPublicId);
        String oldValue = campaign.getName() + "|" + campaign.getCampaignStatus();

        campaign.setName(request.name());
        campaign.setDescription(request.description());
        campaign.setAcademicYear(request.academicYear());
        campaign.setLevel(request.level());
        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());
        if (request.status() != null) {
            campaign.setCampaignStatus(request.status());
        }
        if (request.isGroup() != null) {
            campaign.setIsGroup(com.example.SinhVien5T.campaign.entity.CampaignType.valueOf(request.isGroup().trim().toUpperCase()));
        }

        clearCampaignCaches();
        audit(
                "Campaign",
                campaign.getId(),
                "UPDATE",
                oldValue,
                campaign.getName() + "|" + campaign.getCampaignStatus(),
                authentication
        );

        return toCampaign(campaign);
    }

    @Transactional
    public CampaignResponse changeCampaignStatus(
            String campaignPublicId,
            CampaignStatusRequest request,
            Authentication authentication
    ) {
        Campaign campaign = findCampaign(campaignPublicId);
        CampaignStatus oldStatus = campaign.getCampaignStatus();
        campaign.setCampaignStatus(request.status());

        clearCampaignCaches();
        audit("Campaign", campaign.getId(), "CHANGE_STATUS", oldStatus.name(), request.status().name(), authentication);

        return toCampaign(campaign);
    }

    @Transactional
    public void deleteCampaign(String campaignPublicId, Authentication authentication) {
        Campaign campaign = findCampaign(campaignPublicId);
        campaign.setCampaignStatus(CampaignStatus.ARCHIVED);

        clearCampaignCaches();
        audit("Campaign", campaign.getId(), "DELETE", null, "ARCHIVED", authentication);
    }

    @Transactional(readOnly = true)
    public CampaignStatsResponse getCampaignStats(String campaignPublicId) {
        Campaign campaign = findCampaign(campaignPublicId);
        Long campaignId = campaign.getId();

        long totalParticipants = applicationRecordRepository.countByCampaignId(campaignId);
        long passResults = applicationRecordRepository.countByCampaignIdAndStatus(campaignId, ApplicationStatus.APPROVED);
        long failResults = applicationRecordRepository.countByCampaignIdAndStatus(campaignId, ApplicationStatus.REJECTED);
        long pendingResults = totalParticipants - passResults - failResults;
        long pendingEvidences = evidenceRepository.countByApplicationRecordCampaignIdAndEvidenceStatus(
                campaignId,
                EvidenceStatus.PENDING
        );
        long approvedEvidences = evidenceRepository.countByApplicationRecordCampaignIdAndEvidenceStatus(
                campaignId,
                EvidenceStatus.APPROVED
        );
        long rejectedEvidences = evidenceRepository.countByApplicationRecordCampaignIdAndEvidenceStatus(
                campaignId,
                EvidenceStatus.REJECTED
        );

        return new CampaignStatsResponse(
                campaignPublicId,
                totalParticipants,
                pendingResults,
                passResults,
                failResults,
                pendingEvidences,
                approvedEvidences,
                rejectedEvidences,
                rate(passResults, totalParticipants),
                rate(failResults, totalParticipants)
        );
    }

    @Transactional(readOnly = true)
    public List<StandardResponse> getStandards(String campaignPublicId, Boolean isGroup) {
        Campaign campaign = findCampaign(campaignPublicId);
        List<Standard> standards = isGroup != null
                ? standardRepository.findByCampaignIdAndIsGroupOrderByIdAsc(campaign.getId(), isGroup)
                : standardRepository.findByCampaignIdOrderByIdAsc(campaign.getId());
        return standards.stream()
                .map(this::toStandard)
                .toList();
    }

    @Transactional
    public StandardResponse createStandard(
            String campaignPublicId,
            StandardRequest request,
            Authentication authentication
    ) {
        Campaign campaign = findCampaign(campaignPublicId);

        Level standardLevel = request.level();
        if (standardLevel == null) {
            standardLevel = campaign.getLevel().getDefaultLevel();
        }

        Standard standard = Standard.builder()
                .campaign(campaign)
                .name(request.name())
                .description(request.description())
                .isGroup(request.isGroup() != null ? request.isGroup() : false)
                .level(standardLevel)
                .build();

        Standard saved = standardRepository.save(standard);
        clearCampaignCaches();
        audit("Standard", saved.getId(), "CREATE", null, saved.getName(), authentication);

        return toStandard(saved);
    }

    @Transactional
    public StandardResponse updateStandard(
            String standardPublicId,
            StandardRequest request,
            Authentication authentication
    ) {
        Standard standard = findStandard(standardPublicId);
        String oldValue = standard.getName();

        standard.setName(request.name());
        standard.setDescription(request.description());
        if (request.isGroup() != null) {
            standard.setIsGroup(request.isGroup());
        }
        if (request.level() != null) {
            standard.setLevel(request.level());
        }

        clearCampaignCaches();
        audit("Standard", standard.getId(), "UPDATE", oldValue, standard.getName(), authentication);

        return toStandard(standard);
    }

    @Transactional
    public void deleteStandard(String standardPublicId, Authentication authentication) {
        Standard standard = findStandard(standardPublicId);
        standardRepository.delete(standard);

        clearCampaignCaches();
        audit("Standard", standard.getId(), "DELETE", standard.getName(), null, authentication);
    }

    public void reorderStandard(String standardPublicId, Integer orderIndex) {
        throw new IllegalArgumentException("Chức năng sắp xếp tiêu chuẩn chưa được hỗ trợ");
    }

    @Transactional(readOnly = true)
    public List<CriteriaResponse> getCriteria(String standardPublicId) {
        Standard standard = findStandard(standardPublicId);
        return criteriaRepository.findByStandardIdOrderByOrderIndexAscIdAsc(standard.getId())
                .stream()
                .map(this::toCriteria)
                .toList();
    }

    @Transactional
    public CriteriaResponse createCriteria(
            String standardPublicId,
            CriteriaRequest request,
            Authentication authentication
    ) {
        Standard standard = findStandard(standardPublicId);
        Criteria parent = request.parentPublicId() == null ? null : findCriteria(request.parentPublicId());
        validateParentBelongsToStandard(parent, standard, null);
        validateCriteriaForStandard(request, standard.getId(), null);

        Criteria criteria = Criteria.builder()
                .standard(standard)
                .parent(parent)
                .name(request.name().trim())
                .description(request.description())
                .orderIndex(0)
                .isMandatory(request.mandatory() == null || request.mandatory())
                .requiredChildrenCount(request.requiredChildrenCount() == null ? 0 : request.requiredChildrenCount())
                .evidenceType(request.evidenceType())
                .build();

        Criteria saved = criteriaRepository.save(criteria);
        moveCriteriaToOrder(saved, request.orderIndex());
        clearCampaignCaches();
        audit("Criteria", saved.getId(), "CREATE", null, saved.getName(), authentication);

        return toCriteria(saved);
    }

    @Transactional
    public CriteriaResponse updateCriteria(
            String criteriaPublicId,
            CriteriaRequest request,
            Authentication authentication
    ) {
        Criteria criteria = findCriteria(criteriaPublicId);
        String oldValue = criteria.getName();
        Standard standard = criteria.getStandard();
        Long oldParentId = criteria.getParent() == null ? null : criteria.getParent().getId();
        Criteria parent = request.parentPublicId() == null ? null : findCriteria(request.parentPublicId());

        validateParentBelongsToStandard(parent, standard, criteria.getId());
        validateCriteriaForStandard(request, standard.getId(), criteria.getId());

        criteria.setName(request.name().trim());
        criteria.setDescription(request.description());
        if (request.mandatory() != null) {
            criteria.setIsMandatory(request.mandatory());
        }
        if (request.requiredChildrenCount() != null) {
            criteria.setRequiredChildrenCount(request.requiredChildrenCount());
        }
        criteria.setEvidenceType(request.evidenceType());
        criteria.setParent(parent);

        Integer requestedOrder = request.orderIndex() == null ? criteria.getOrderIndex() : request.orderIndex();
        normalizeCriteriaOrder(standard.getId(), oldParentId);
        moveCriteriaToOrder(criteria, requestedOrder);

        clearCampaignCaches();
        audit("Criteria", criteria.getId(), "UPDATE", oldValue, criteria.getName(), authentication);

        return toCriteria(criteria);
    }

    @Transactional
    public CriteriaResponse updateCriteriaRequirement(
            String criteriaPublicId,
            CriteriaRequirementRequest request,
            Authentication authentication
    ) {
        Criteria criteria = findCriteria(criteriaPublicId);
        String oldValue = criteria.getIsMandatory() + "|" + criteria.getRequiredChildrenCount() + "|" + criteria.getEvidenceType();

        if (request.mandatory() != null) {
            criteria.setIsMandatory(request.mandatory());
        }
        if (request.requiredChildrenCount() != null) {
            criteria.setRequiredChildrenCount(request.requiredChildrenCount());
        }
        if (request.evidenceType() != null) {
            criteria.setEvidenceType(request.evidenceType());
        }

        clearCampaignCaches();
        audit(
                "Criteria",
                criteria.getId(),
                "UPDATE",
                oldValue,
                criteria.getIsMandatory() + "|" + criteria.getRequiredChildrenCount() + "|" + criteria.getEvidenceType(),
                authentication
        );

        return toCriteria(criteria);
    }

    @Transactional
    public void deleteCriteria(String criteriaPublicId, Authentication authentication) {
        Criteria criteria = findCriteria(criteriaPublicId);
        if (criteriaRepository.countByParentId(criteria.getId()) > 0 || evidenceRepository.countByCriteriaId(criteria.getId()) > 0) {
            throw new IllegalArgumentException("Tiêu chí đã có tiêu chí con hoặc minh chứng, không thể xóa");
        }

        Long standardId = criteria.getStandard().getId();
        Long parentId = criteria.getParent() == null ? null : criteria.getParent().getId();
        criteriaRepository.delete(criteria);
        criteriaRepository.flush();
        normalizeCriteriaOrder(standardId, parentId);

        clearCampaignCaches();
        audit("Criteria", criteria.getId(), "DELETE", criteria.getName(), null, authentication);
    }

    @Transactional
    public void reorderCriteria(String criteriaPublicId, Integer orderIndex) {
        if (orderIndex == null || orderIndex < 1) {
            throw new IllegalArgumentException("Thứ tự tiêu chí phải lớn hơn 0");
        }

        Criteria criteria = findCriteria(criteriaPublicId);
        moveCriteriaToOrder(criteria, orderIndex);
        clearCampaignCaches();
    }

    @Transactional
    public EvidenceResponse submitEvidence(SaveEvidenceRequest request, Authentication authentication) {
        CustomUserDetails currentUser = currentUser(authentication);
        Criteria criteria = findCriteria(request.getCriteriaPublicId());
        Campaign campaign = criteria.getStandard().getCampaign();

        if (criteria.getEvidenceType() == EvidenceType.NONE) {
            throw new IllegalArgumentException("Tiêu chí này không yêu cầu minh chứng");
        }

        ApplicationRecord record = applicationRecordRepository.findByUserIdAndCampaignId(
                        currentUser.getId(),
                        campaign.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký hồ sơ cho đợt xét chọn này"));

        if (record.getStatus() == ApplicationStatus.APPROVED || record.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Hồ sơ đã có kết quả, không thể nộp thêm minh chứng");
        }

        Evidence evidence = evidenceRepository.findByApplicationRecordIdAndCriteriaId(record.getId(), criteria.getId())
                .orElseGet(Evidence::new);
        boolean isCreate = evidence.getId() == null;

        evidence.setApplicationRecord(record);
        evidence.setCriteria(criteria);
        evidence.setEvidenceUrl(request.getEvidenceUrl().trim());
        evidence.setEvidenceStatus(EvidenceStatus.PENDING);
        evidence.setReviewerComment(null);

        Evidence saved = evidenceRepository.save(evidence);
        clearCampaignCaches();
        audit("Evidence", saved.getId(), isCreate ? "CREATE" : "UPDATE", null, saved.getEvidenceUrl(), authentication);

        return toEvidence(saved);
    }

    @Transactional(readOnly = true)
    public List<EvidenceResponse> getMyEvidences(Authentication authentication) {
        return evidenceRepository.findByApplicationRecordUserIdOrderByIdDesc(currentUser(authentication).getId())
                .stream()
                .map(this::toEvidence)
                .toList();
    }

    @Transactional(readOnly = true)
    public EvidenceResponse getMyEvidence(String evidencePublicId, Authentication authentication) {
        Evidence evidence = findEvidence(evidencePublicId);
        if (!evidence.getApplicationRecord().getUser().getId().equals(currentUser(authentication).getId())) {
            throw new ResourceNotFoundException("Không tìm thấy minh chứng");
        }

        return toEvidence(evidence);
    }

    @Transactional(readOnly = true)
    public PageResponse<EvidenceResponse> getAdminEvidences(
            int page,
            int size,
            EvidenceStatus status,
            String campaignPublicId,
            String keyword
    ) {
        Long campaignId = null;
        if (campaignPublicId != null && !campaignPublicId.isBlank()) {
            campaignId = findCampaign(campaignPublicId).getId();
        }

        return toPage(
                evidenceRepository.search(status, campaignId, like(keyword), PageRequest.of(page, size)),
                this::toEvidence
        );
    }

    @Transactional(readOnly = true)
    public EvidenceResponse getAdminEvidence(String evidencePublicId) {
        return toEvidence(findEvidence(evidencePublicId));
    }

    @Transactional
    public EvidenceResponse approveEvidence(String evidencePublicId, String comment, Authentication authentication) {
        Evidence evidence = findEvidence(evidencePublicId);
        EvidenceStatus oldStatus = evidence.getEvidenceStatus();

        evidence.setEvidenceStatus(EvidenceStatus.APPROVED);
        evidence.setReviewerComment(comment);
        updateApplicationRecordStatus(evidence.getApplicationRecord(), authentication);

        clearCampaignCaches();
        audit("Evidence", evidence.getId(), "APPROVE", oldStatus.name(), "APPROVED", authentication);

        return toEvidence(evidence);
    }

    @Transactional
    public EvidenceResponse approveEvidence(String evidencePublicId, Authentication authentication) {
        return approveEvidence(evidencePublicId, null, authentication);
    }

    @Transactional
    public EvidenceResponse updateReviewerComment(String evidencePublicId, String comment, Authentication authentication) {
        Evidence evidence = findEvidence(evidencePublicId);
        String oldComment = evidence.getReviewerComment();
        evidence.setReviewerComment(comment);

        clearCampaignCaches();
        audit("Evidence", evidence.getId(), "UPDATE_COMMENT", oldComment, comment, authentication);

        return toEvidence(evidence);
    }

    @Transactional
    public EvidenceResponse rejectEvidence(
            String evidencePublicId,
            RejectRequest request,
            Authentication authentication
    ) {
        Evidence evidence = findEvidence(evidencePublicId);
        EvidenceStatus oldStatus = evidence.getEvidenceStatus();

        evidence.setEvidenceStatus(EvidenceStatus.REJECTED);
        evidence.setReviewerComment(request.reason());
        updateApplicationRecordStatus(evidence.getApplicationRecord(), authentication);

        clearCampaignCaches();
        audit("Evidence", evidence.getId(), "REJECT", oldStatus.name(), request.reason(), authentication);

        return toEvidence(evidence);
    }

    @Transactional
    public List<EvidenceResponse> bulkApprove(IdsRequest request, Authentication authentication) {
        return request.publicIds().stream()
                .map(publicId -> approveEvidence(publicId, authentication))
                .toList();
    }

    @Transactional
    public List<EvidenceResponse> bulkReject(
            IdsRequest request,
            String reason,
            Authentication authentication
    ) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Lý do từ chối không được để trống");
        }

        RejectRequest rejectRequest = new RejectRequest(reason);
        return request.publicIds().stream()
                .map(publicId -> rejectEvidence(publicId, rejectRequest, authentication))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ResultResponse> getCampaignResults(
            String campaignPublicId,
            String status,
            String keyword,
            Boolean isGroup,
            Level level,
            int page,
            int size
    ) {
        String cleanCampaignPublicId = (campaignPublicId == null || campaignPublicId.isBlank() || campaignPublicId.equals("undefined")) ? null : campaignPublicId;
        if (cleanCampaignPublicId != null) {
            findCampaign(cleanCampaignPublicId);
        }
        return toPage(
                applicationRecordRepository.searchResults(
                        cleanCampaignPublicId,
                        toApplicationStatuses(status),
                        like(keyword),
                        isGroup,
                        level,
                        PageRequest.of(page, size)
                ),
                this::toResult
        );
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> getUserResults(String userPublicId) {
        findUser(userPublicId);
        return applicationRecordRepository.findByUserPublicIdOrderByCreatedAtDesc(userPublicId)
                .stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResultDetailResponse getUserCampaignResult(
            String userPublicId,
            String campaignPublicId
    ) {
        ApplicationRecord record = applicationRecordRepository.findResultDetail(userPublicId, campaignPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay ket qua"));

        Campaign campaign = record.getCampaign();
        Map<Long, Evidence> evidenceByCriteriaId = record.getEvidences().stream()
                .collect(Collectors.toMap(
                        evidence -> evidence.getCriteria().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));

        List<ResultDetailResponse.CriteriaResult> criteriaResults = criteriaRepository.findAllByCampaignIdAndIsGroup(
                        campaign.getId(),
                        record.getIsGroup() != null ? record.getIsGroup() : false
                )
                .stream()
                .map(criteria -> {
                    Evidence evidence = evidenceByCriteriaId.get(criteria.getId());
                    return new ResultDetailResponse.CriteriaResult(
                            criteria.getPublicId(),
                            criteria.getName(),
                            criteria.getIsMandatory(),
                            criteria.getEvidenceType() == null ? null : criteria.getEvidenceType().name(),
                            evidence == null ? null : evidence.getEvidenceStatus().name(),
                            evidence == null ? null : evidence.getEvidenceUrl(),
                            evidence == null ? null : evidence.getReviewerComment()
                    );
                })
                .toList();

        return new ResultDetailResponse(
                record.getUser().getPublicId(),
                campaign.getPublicId(),
                campaign.getName(),
                toBusinessStatus(record.getStatus()),
                criteriaResults
        );
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> getSettings() {
        return settingRepository.findAll().stream()
                .map(setting -> new SettingResponse(
                        setting.getKeyName(),
                        setting.getValue(),
                        setting.getDescription()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public SettingResponse getSetting(String key) {
        SystemSetting setting = findSetting(key);
        return new SettingResponse(setting.getKeyName(), setting.getValue(), setting.getDescription());
    }

    @Transactional
    public SettingResponse createSetting(SettingRequest request, Authentication authentication) {
        if (request.keyName() == null || request.keyName().isBlank()) {
            throw new IllegalArgumentException("Mã cấu hình không được để trống");
        }
        if (settingRepository.existsById(request.keyName())) {
            throw new IllegalArgumentException("Cấu hình đã tồn tại");
        }

        SystemSetting setting = SystemSetting.builder()
                .keyName(request.keyName())
                .value(request.value())
                .description(request.description())
                .build();

        SystemSetting saved = settingRepository.save(setting);
        audit("SystemSetting", null, "CREATE", null, saved.getKeyName(), authentication);

        return new SettingResponse(saved.getKeyName(), saved.getValue(), saved.getDescription());
    }

    @Transactional
    public SettingResponse updateSetting(
            String key,
            SettingRequest request,
            Authentication authentication
    ) {
        SystemSetting setting = findSetting(key);
        String oldValue = setting.getValue();

        setting.setValue(request.value());
        setting.setDescription(request.description());
        audit("SystemSetting", null, "UPDATE", oldValue, setting.getValue(), authentication);

        return new SettingResponse(setting.getKeyName(), setting.getValue(), setting.getDescription());
    }

    @Transactional
    public void deleteSetting(String key, Authentication authentication) {
        SystemSetting setting = findSetting(key);
        settingRepository.delete(setting);
        audit("SystemSetting", null, "DELETE", setting.getKeyName(), null, authentication);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(LocalDate from, LocalDate to) {
        long totalUsers = userRepository.count();
        long activeCampaigns = campaignRepository.countByCampaignStatus(CampaignStatus.ACTIVE);
        long pendingEvidences;
        long passResults;
        long failResults;

        if (from == null && to == null) {
            pendingEvidences = evidenceRepository.countByEvidenceStatus(EvidenceStatus.PENDING);
            passResults = applicationRecordRepository.countByStatus(ApplicationStatus.APPROVED);
            failResults = applicationRecordRepository.countByStatus(ApplicationStatus.REJECTED);
        } else {
            LocalDateTime start = (from == null ? LocalDate.of(1970, 1, 1) : from).atStartOfDay();
            LocalDateTime end = (to == null ? LocalDate.now() : to).atTime(LocalTime.MAX);

            pendingEvidences = evidenceRepository.countByStatusAndApplicationCreatedAtBetween(
                    EvidenceStatus.PENDING,
                    start,
                    end
            );
            passResults = applicationRecordRepository.countByStatusAndCreatedAtBetween(
                    ApplicationStatus.APPROVED,
                    start,
                    end
            );
            failResults = applicationRecordRepository.countByStatusAndCreatedAtBetween(
                    ApplicationStatus.REJECTED,
                    start,
                    end
            );
        }

        long totalFinalResults = passResults + failResults;
        return new DashboardResponse(
                totalUsers,
                activeCampaigns,
                pendingEvidences,
                rate(passResults, totalFinalResults),
                rate(failResults, totalFinalResults)
        );
    }

    public List<Role> getRoles() {
        return List.of(Role.ADMIN, Role.MENTOR, Role.USER);
    }

    @Transactional(readOnly = true)
    public List<Permission> getPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional
    public void updateUserPermissions(
            String userPublicId,
            UpdatePermissionRequest request,
            Authentication authentication
    ) {
        User user = findUser(userPublicId);
        Set<String> requestedPermissionCodes = new LinkedHashSet<>(request.permissions());
        List<Permission> requestedPermissions = permissionRepository.findAllById(requestedPermissionCodes);

        if (requestedPermissions.size() != requestedPermissionCodes.size()) {
            throw new IllegalArgumentException("Danh sách quyền không hợp lệ");
        }

        List<UserPermission> currentPermissions = userPermissionRepository.findByUserId(user.getId());
        Set<String> existingPermissionCodes = currentPermissions.stream()
                .map(userPermission -> userPermission.getPermission().getCode())
                .collect(Collectors.toSet());

        for (UserPermission currentPermission : currentPermissions) {
            if (!requestedPermissionCodes.contains(currentPermission.getPermission().getCode())) {
                userPermissionRepository.delete(currentPermission);
            }
        }

        for (Permission permission : requestedPermissions) {
            if (!existingPermissionCodes.contains(permission.getCode())) {
                userPermissionRepository.save(UserPermission.builder()
                        .user(user)
                        .permission(permission)
                        .build());
            }
        }

        cachedUserPrincipalService.evict(user.getId());
        audit(
                "UserPermission",
                user.getId(),
                "UPDATE",
                null,
                String.join(",", requestedPermissionCodes),
                authentication
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLog> getAuditLogs(
            int page,
            int size,
            String entity,
            String action,
            Long userId
    ) {
        return new PageResponse<>(List.of(), page, size, 0L, 0);
    }

    private void updateApplicationRecordStatus(
            ApplicationRecord record,
            Authentication authentication
    ) {
        ApplicationStatus oldStatus = record.getStatus();
        List<Criteria> criteriaList = criteriaRepository.findAllByCampaignIdAndIsGroup(
                record.getCampaign().getId(),
                record.getIsGroup() != null ? record.getIsGroup() : false
        );

        if (record.getCampaign().getLevel().isMultiLevel()) {
            final Level finalLevel = record.getLevel() != null ? record.getLevel() : Level.UNIVERSITY;
            criteriaList = criteriaList.stream()
                    .filter(c -> c.getStandard().getLevel() == finalLevel)
                    .toList();
        }

        Map<Long, List<Criteria>> childrenByParentId = criteriaList.stream()
                .filter(criteria -> criteria.getParent() != null)
                .collect(Collectors.groupingBy(criteria -> criteria.getParent().getId()));

        Map<Long, Evidence> evidenceByCriteriaId = evidenceRepository.findByApplicationRecordId(record.getId())
                .stream()
                .collect(Collectors.toMap(
                        evidence -> evidence.getCriteria().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));

        boolean hasMandatoryRejected = criteriaList.stream()
                .filter(criteria -> Boolean.TRUE.equals(criteria.getIsMandatory()))
                .map(criteria -> evidenceByCriteriaId.get(criteria.getId()))
                .anyMatch(evidence -> evidence != null && evidence.getEvidenceStatus() == EvidenceStatus.REJECTED);

        boolean allMandatoryPassed = criteriaList.stream()
                .filter(criteria -> Boolean.TRUE.equals(criteria.getIsMandatory()))
                .allMatch(criteria -> isCriteriaPassed(
                        criteria,
                        childrenByParentId,
                        evidenceByCriteriaId,
                        new HashMap<>()
                ));

        ApplicationStatus newStatus = hasMandatoryRejected
                ? ApplicationStatus.REJECTED
                : allMandatoryPassed ? ApplicationStatus.APPROVED : ApplicationStatus.SUBMITTED;

        record.setStatus(newStatus);
        if (oldStatus != newStatus) {
            audit("ApplicationRecord", record.getId(), "CHANGE_STATUS", oldStatus.name(), newStatus.name(), authentication);
            publishApplicationStatusNotification(record, newStatus);
        }
    }

    private void publishApplicationStatusNotification(ApplicationRecord record, ApplicationStatus status) {
        NotificationType type = switch (status) {
            case APPROVED -> NotificationType.APPROVED;
            case REJECTED -> NotificationType.REJECTED;
            default -> null;
        };
        if (type == null) {
            return;
        }

        notificationEventPublisher.publishAfterCommit(new NotificationEvent(
                record.getUser().getId(),
                type,
                Map.of(
                        "studentName", displayName(record.getUser()),
                        "campaignName", record.getCampaign().getName(),
                        "status", status.name(),
                        "reason", rejectionReason(record)
                ),
                "ApplicationRecord",
                record.getPublicId()
        ));
    }

    private String rejectionReason(ApplicationRecord record) {
        return record.getEvidences().stream()
                .filter(evidence -> evidence.getEvidenceStatus() == EvidenceStatus.REJECTED)
                .map(Evidence::getReviewerComment)
                .filter(comment -> comment != null && !comment.isBlank())
                .findFirst()
                .orElse("");
    }

    private String displayName(User user) {
        UserDetail detail = user.getDetail();
        if (detail != null && detail.getFullName() != null && !detail.getFullName().isBlank()) {
            return detail.getFullName();
        }
        return user.getEmail();
    }

    private boolean isCriteriaPassed(
            Criteria criteria,
            Map<Long, List<Criteria>> childrenByParentId,
            Map<Long, Evidence> evidenceByCriteriaId,
            Map<Long, Boolean> memo
    ) {
        Boolean cached = memo.get(criteria.getId());
        if (cached != null) {
            return cached;
        }

        Evidence evidence = evidenceByCriteriaId.get(criteria.getId());
        boolean directPassed = !requiresEvidence(criteria)
                || (evidence != null && evidence.getEvidenceStatus() == EvidenceStatus.APPROVED);

        int requiredChildrenCount = criteria.getRequiredChildrenCount() == null
                ? 0
                : criteria.getRequiredChildrenCount();
        boolean childrenPassed = true;

        if (requiredChildrenCount > 0) {
            long passedChildren = childrenByParentId.getOrDefault(criteria.getId(), List.of())
                    .stream()
                    .filter(child -> isCriteriaPassed(child, childrenByParentId, evidenceByCriteriaId, memo))
                    .count();
            childrenPassed = passedChildren >= requiredChildrenCount;
        }

        boolean passed = directPassed && childrenPassed;
        memo.put(criteria.getId(), passed);

        return passed;
    }

    private boolean requiresEvidence(Criteria criteria) {
        return criteria.getEvidenceType() != null && criteria.getEvidenceType() != EvidenceType.NONE;
    }

    private Collection<ApplicationStatus> toApplicationStatuses(String status) {
        if (status == null || status.isBlank()) {
            return List.of(
                    ApplicationStatus.DRAFT,
                    ApplicationStatus.SUBMITTED,
                    ApplicationStatus.APPROVED,
                    ApplicationStatus.REJECTED
            );
        }

        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> List.of(ApplicationStatus.DRAFT, ApplicationStatus.SUBMITTED);
            case "PASS", "APPROVED" -> List.of(ApplicationStatus.APPROVED);
            case "FAIL", "REJECTED" -> List.of(ApplicationStatus.REJECTED);
            case "DRAFT" -> List.of(ApplicationStatus.DRAFT);
            case "SUBMITTED" -> List.of(ApplicationStatus.SUBMITTED);
            default -> throw new IllegalArgumentException("Trạng thái kết quả không hợp lệ");
        };
    }

    private String toBusinessStatus(ApplicationStatus status) {
        return switch (status) {
            case APPROVED -> "PASS";
            case REJECTED -> "FAIL";
            default -> "PENDING";
        };
    }

    private UserAdminResponse toUser(User user) {
        UserDetail detail = user.getDetail();
        return new UserAdminResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isVerified(),
                detail == null ? null : detail.getFullName(),
                detail == null ? null : detail.getStudentCode(),
                detail == null ? null : detail.getPhoneNumber(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private CampaignResponse toCampaign(Campaign campaign) {
        return new CampaignResponse(
                campaign.getPublicId(),
                campaign.getName(),
                campaign.getDescription(),
                campaign.getAcademicYear(),
                campaign.getLevel().name(),
                campaign.getCampaignStatus().name(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                criteriaRepository.countByStandardCampaignId(campaign.getId()),
                campaign.getIsGroup() != null ? campaign.getIsGroup().name() : com.example.SinhVien5T.campaign.entity.CampaignType.INDIVIDUAL.name()
        );
    }

    private StandardResponse toStandard(Standard standard) {
        return new StandardResponse(
                standard.getPublicId(),
                standard.getName(),
                standard.getDescription(),
                standard.getCampaign() != null ? standard.getCampaign().getPublicId() : null,
                criteriaRepository.countByStandardId(standard.getId()),
                standard.getIsGroup(),
                standard.getLevel() != null ? standard.getLevel().name() : null
        );
    }

    private CriteriaResponse toCriteria(Criteria criteria) {
        return new CriteriaResponse(
                criteria.getPublicId(),
                criteria.getName(),
                criteria.getDescription(),
                criteria.getOrderIndex(),
                criteria.getIsMandatory(),
                criteria.getRequiredChildrenCount(),
                criteria.getEvidenceType() == null ? null : criteria.getEvidenceType().name(),
                criteria.getParent() == null ? null : criteria.getParent().getPublicId()
        );
    }

    private EvidenceResponse toEvidence(Evidence evidence) {
        ApplicationRecord record = evidence.getApplicationRecord();
        User user = record.getUser();
        UserDetail detail = user.getDetail();
        Campaign campaign = record.getCampaign();
        Criteria criteria = evidence.getCriteria();

        return new EvidenceResponse(
                evidence.getPublicId(),
                evidence.getEvidenceUrl(),
                evidence.getEvidenceOriginalFilename(),
                evidence.getEvidenceFormat(),
                evidence.getEvidenceStatus().name(),
                evidence.getReviewerComment(),
                criteria.getPublicId(),
                criteria.getName(),
                user.getPublicId(),
                user.getEmail(),
                detail == null ? null : detail.getFullName(),
                campaign.getPublicId(),
                campaign.getName()
        );
    }

    private ResultResponse toResult(ApplicationRecord record) {
        User user = record.getUser();
        UserDetail detail = user.getDetail();
        Campaign campaign = record.getCampaign();

        return new ResultResponse(
                record.getPublicId(),
                user.getPublicId(),
                user.getEmail(),
                detail == null ? null : detail.getFullName(),
                detail == null ? null : detail.getStudentCode(),
                campaign.getPublicId(),
                campaign.getName(),
                record.getLevel(),
                toBusinessStatus(record.getStatus()),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getEvidences().size()
        );
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

    private void validateCriteriaForStandard(
            CriteriaRequest request,
            Long standardId,
            Long excludedCriteriaId
    ) {
        String name = request.name().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên tiêu chí không được để trống");
        }

        boolean nameExists = excludedCriteriaId == null
                ? criteriaRepository.existsByNameIgnoreCaseAndStandardId(name, standardId)
                : criteriaRepository.existsByNameIgnoreCaseAndStandardIdAndIdNot(name, standardId, excludedCriteriaId);
        if (nameExists) {
            throw new IllegalArgumentException("Tên tiêu chí đã tồn tại trong tiêu chuẩn này");
        }
    }

    private void validateParentBelongsToStandard(Criteria parent, Standard standard, Long criteriaId) {
        if (parent == null) {
            return;
        }
        if (criteriaId != null && parent.getId().equals(criteriaId)) {
            throw new IllegalArgumentException("Tiêu chí không thể tự làm tiêu chí cha của chính nó");
        }
        if (!parent.getStandard().getId().equals(standard.getId())) {
            throw new IllegalArgumentException("Tiêu chí cha phải thuộc cùng tiêu chuẩn");
        }
    }

    private void moveCriteriaToOrder(Criteria criteria, Integer requestedOrder) {
        Long standardId = criteria.getStandard().getId();
        Long parentId = criteria.getParent() == null ? null : criteria.getParent().getId();
        List<Criteria> siblings = new ArrayList<>(findCriteriaSiblings(standardId, parentId));

        siblings.removeIf(item -> item.getId().equals(criteria.getId()));
        int targetIndex = requestedOrder == null
                ? siblings.size()
                : Math.max(0, Math.min(requestedOrder - 1, siblings.size()));

        siblings.add(targetIndex, criteria);
        applyCriteriaOrder(siblings);
    }

    private void normalizeCriteriaOrder(Long standardId, Long parentId) {
        applyCriteriaOrder(findCriteriaSiblings(standardId, parentId));
    }

    private List<Criteria> findCriteriaSiblings(Long standardId, Long parentId) {
        return parentId == null
                ? criteriaRepository.findByStandardIdAndParentIsNullOrderByOrderIndexAscIdAsc(standardId)
                : criteriaRepository.findByStandardIdAndParentIdOrderByOrderIndexAscIdAsc(standardId, parentId);
    }

    private void applyCriteriaOrder(List<Criteria> criteriaList) {
        for (int index = 0; index < criteriaList.size(); index++) {
            criteriaList.get(index).setOrderIndex(index + 1);
        }
    }

    private User findUser(String publicId) {
        return userRepository.findByPublicIdWithDetail(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private Campaign findCampaign(String publicId) {
        return campaignRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));
    }

    private Standard findStandard(String publicId) {
        return standardRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiêu chuẩn"));
    }

    private Criteria findCriteria(String publicId) {
        return criteriaRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiêu chí"));
    }

    private Evidence findEvidence(String publicId) {
        return evidenceRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy minh chứng"));
    }

    private SystemSetting findSetting(String key) {
        return settingRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình"));
    }

    private void applyCreateDetail(User user, AdminUserCreateRequest request) {
        if (!hasAnyDetail(
                request.fullName(),
                request.studentCode(),
                request.phoneNumber(),
                request.birthDay(),
                request.gender(),
                request.identityCardNumber(),
                request.ethnicity(),
                request.school(),
                request.major(),
                request.academicYear(),
                request.administrativeClass(),
                request.faculty(),
                request.currentPosition(),
                request.contactEmail(),
                request.unionPosition(),
                request.politicalStatus()
        )) {
            return;
        }

        requireFullDetail(
                request.fullName(),
                request.studentCode(),
                request.phoneNumber(),
                request.birthDay(),
                request.gender(),
                request.identityCardNumber(),
                request.ethnicity(),
                request.school(),
                request.academicYear(),
                request.administrativeClass(),
                request.faculty(),
                request.currentPosition(),
                request.politicalStatus()
        );

        UserDetail detail = UserDetail.builder()
                .fullName(request.fullName())
                .birthDay(request.birthDay())
                .gender(request.gender())
                .identityCardNumber(request.identityCardNumber())
                .ethnicity(request.ethnicity())
                .school(request.school())
                .major(request.major())
                .academicYear(request.academicYear())
                .studentCode(request.studentCode())
                .administrativeClass(request.administrativeClass())
                .faculty(request.faculty())
                .currentPosition(request.currentPosition())
                .contactEmail(isBlank(request.contactEmail()) ? user.getEmail() : request.contactEmail())
                .phoneNumber(request.phoneNumber())
                .unionPosition(request.unionPosition())
                .politicalStatus(request.politicalStatus())
                .build();

        user.setDetail(detail);
    }

    private void applyUpdateDetail(User user, AdminUserUpdateRequest request) {
        if (!hasAnyDetail(
                request.fullName(),
                request.studentCode(),
                request.phoneNumber(),
                request.birthDay(),
                request.gender(),
                request.identityCardNumber(),
                request.ethnicity(),
                request.school(),
                request.major(),
                request.academicYear(),
                request.administrativeClass(),
                request.faculty(),
                request.currentPosition(),
                request.contactEmail(),
                request.unionPosition(),
                request.politicalStatus()
        )) {
            return;
        }

        if (user.getDetail() == null) {
            requireFullDetail(
                    request.fullName(),
                    request.studentCode(),
                    request.phoneNumber(),
                    request.birthDay(),
                    request.gender(),
                    request.identityCardNumber(),
                    request.ethnicity(),
                    request.school(),
                    request.academicYear(),
                    request.administrativeClass(),
                    request.faculty(),
                    request.currentPosition(),
                    request.politicalStatus()
            );

            UserDetail detail = UserDetail.builder()
                    .fullName(request.fullName())
                    .birthDay(request.birthDay())
                    .gender(request.gender())
                    .identityCardNumber(request.identityCardNumber())
                    .ethnicity(request.ethnicity())
                    .school(request.school())
                    .major(request.major())
                    .academicYear(request.academicYear())
                    .studentCode(request.studentCode())
                    .administrativeClass(request.administrativeClass())
                    .faculty(request.faculty())
                    .currentPosition(request.currentPosition())
                    .contactEmail(isBlank(request.contactEmail()) ? user.getEmail() : request.contactEmail())
                    .phoneNumber(request.phoneNumber())
                    .unionPosition(request.unionPosition())
                    .politicalStatus(request.politicalStatus())
                    .build();

            user.setDetail(detail);
            return;
        }

        UserDetail detail = user.getDetail();
        if (request.fullName() != null) detail.setFullName(request.fullName());
        if (request.studentCode() != null) detail.setStudentCode(request.studentCode());
        if (request.phoneNumber() != null) detail.setPhoneNumber(request.phoneNumber());
        if (request.birthDay() != null) detail.setBirthDay(request.birthDay());
        if (request.gender() != null) detail.setGender(request.gender());
        if (request.identityCardNumber() != null) detail.setIdentityCardNumber(request.identityCardNumber());
        if (request.ethnicity() != null) detail.setEthnicity(request.ethnicity());
        if (request.school() != null) detail.setSchool(request.school());
        if (request.major() != null) detail.setMajor(request.major());
        if (request.academicYear() != null) detail.setAcademicYear(request.academicYear());
        if (request.administrativeClass() != null) detail.setAdministrativeClass(request.administrativeClass());
        if (request.faculty() != null) detail.setFaculty(request.faculty());
        if (request.currentPosition() != null) detail.setCurrentPosition(request.currentPosition());
        if (request.contactEmail() != null) detail.setContactEmail(request.contactEmail());
        if (request.unionPosition() != null) detail.setUnionPosition(request.unionPosition());
        if (request.politicalStatus() != null) detail.setPoliticalStatus(request.politicalStatus());
    }

    private void requireFullDetail(
            String fullName,
            String studentCode,
            String phoneNumber,
            Object birthDay,
            Object gender,
            String identityCardNumber,
            String ethnicity,
            String school,
            Integer academicYear,
            String administrativeClass,
            String faculty,
            String currentPosition,
            Object politicalStatus
    ) {
        if (isBlank(fullName)
                || isBlank(studentCode)
                || isBlank(phoneNumber)
                || birthDay == null
                || gender == null
                || isBlank(identityCardNumber)
                || isBlank(ethnicity)
                || isBlank(school)
                || academicYear == null
                || isBlank(administrativeClass)
                || isBlank(faculty)
                || isBlank(currentPosition)
                || politicalStatus == null) {
            throw new IllegalArgumentException("Vui lòng cung cấp đầy đủ thông tin bắt buộc của người dùng");
        }
    }

    private boolean hasAnyDetail(Object... values) {
        return Arrays.stream(values)
                .anyMatch(value -> value != null && (!(value instanceof String text) || !text.isBlank()));
    }

    private void audit(
            String entity,
            Long entityId,
            String action,
            String oldValue,
            String newValue,
            Authentication authentication
    ) {
        auditLogRepository.save(AuditLog.builder()
                .entity(entity)
                .entityId(entityId)
                .action(action)
                .actorId(actorId(authentication))
                .actorEmail(actorEmail(authentication))
                .oldValue(oldValue)
                .newValue(newValue)
                .build());
    }

    private CustomUserDetails currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập");
        }
        return user;
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

    private String like(String keyword) {
        return keyword == null || keyword.isBlank()
                ? null
                : keyword.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private double rate(long count, long total) {
        return total == 0 ? 0 : Math.round(count * 10000.0 / total) / 100.0;
    }

    private void clearCampaignCaches() {
        clear(CacheConfig.CAMPAIGN_CURRENT);
        clear(CacheConfig.CAMPAIGN_DETAIL);
        clear(CacheConfig.CAMPAIGN_CRITERIA);
        clear(CacheConfig.CAMPAIGN_CRITERIA_USER);
        clear(CacheConfig.APPLICATION_RECORD_MINE);
        clear(CacheConfig.DASHBOARD_ADMIN);
        clear(CacheConfig.DASHBOARD_CAMPAIGN);
    }
    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<StandardDTO> getUserCriteriaTree(
            String userPublicId,
            String campaignPublicId,
            Boolean isGroup,
            Level level
    ) {
        User user = findUser(userPublicId);
        Campaign campaign = findCampaign(campaignPublicId);
        boolean isGroupVal = isGroup != null ? isGroup : false;

        Optional<ApplicationRecord> recordOptional;
        if (level != null) {
            recordOptional = applicationRecordRepository
                    .findByUserIdAndCampaignIdAndIsGroupAndLevel(user.getId(), campaign.getId(), isGroupVal, level);
        } else {
            List<ApplicationRecord> records = applicationRecordRepository
                    .findByUserIdAndCampaignIdAndIsGroupOrderByCreatedAtDesc(user.getId(), campaign.getId(), isGroupVal);
            recordOptional = records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
        }

        Level recordLevel = null;
        if (recordOptional.isPresent()) {
            recordLevel = recordOptional.get().getLevel();
        } else if (level != null) {
            recordLevel = level;
        } else {
            recordLevel = campaign.getLevel().getDefaultLevel();
        }

        Map<Long, Evidence> evidenceMap = new HashMap<>();

        if (recordOptional.isPresent()) {
            List<Evidence> evidences = evidenceRepository.findByApplicationRecordId(recordOptional.get().getId());
            evidenceMap.putAll(
                    evidences.stream().collect(Collectors.toMap(
                            evidence -> evidence.getCriteria().getId(),
                            evidence -> evidence,
                            (left, right) -> left
                    ))
            );
        }

        List<Criteria> criteriaList = criteriaRepository.findAllByCampaignIdAndIsGroup(campaign.getId(), isGroupVal);

        if (campaign.getLevel().isMultiLevel()) {
            final Level finalLevel = recordLevel;
            criteriaList = criteriaList.stream()
                    .filter(c -> c.getStandard().getLevel() == finalLevel)
                    .toList();
        }

        Map<Long, CriteriaDTO> criteriaDTOMap = criteriaList.stream()
                .collect(Collectors.toMap(
                        Criteria::getId,
                        criteria -> convertToCriteriaDTO(criteria, evidenceMap)
                ));

        for (Criteria criteria : criteriaList) {
            if (criteria.getParent() != null) {
                CriteriaDTO parentDTO = criteriaDTOMap.get(criteria.getParent().getId());
                CriteriaDTO childDTO = criteriaDTOMap.get(criteria.getId());

                if (parentDTO != null && childDTO != null) {
                    parentDTO.getSubCriteriaList().add(childDTO);
                }
            }
        }

        Map<Long, List<CriteriaDTO>> rootCriteriaMap = criteriaList.stream()
                .filter(criteria -> criteria.getParent() == null)
                .collect(Collectors.groupingBy(
                        criteria -> criteria.getStandard().getId(),
                        Collectors.mapping(
                                criteria -> criteriaDTOMap.get(criteria.getId()),
                                Collectors.toList()
                        )
                ));

        List<Standard> standards = standardRepository.findByCampaignIdAndIsGroupOrderByIdAsc(campaign.getId(), isGroupVal);
        if (campaign.getLevel().isMultiLevel()) {
            final Level finalLevel = recordLevel;
            standards = standards.stream()
                    .filter(s -> s.getLevel() == finalLevel)
                    .toList();
        }

        return standards.stream()
                .map(standard -> StandardDTO.builder()
                        .publicId(standard.getPublicId())
                        .name(standard.getName())
                        .description(standard.getDescription())
                        .criteriaDTOList(rootCriteriaMap.getOrDefault(standard.getId(), new ArrayList<>()))
                        .build())
                .toList();
    }

    private CriteriaDTO convertToCriteriaDTO(
            Criteria criteria,
            Map<Long, Evidence> evidenceMap
    ) {
        Evidence evidence = evidenceMap.get(criteria.getId());

        return CriteriaDTO.builder()
                .publicId(criteria.getPublicId())
                .name(criteria.getName())
                .description(criteria.getDescription())
                .isMandatory(criteria.getIsMandatory())
                .requiredChildrenCount(criteria.getRequiredChildrenCount())
                .evidenceType(
                        criteria.getEvidenceType() == null
                                ? null
                                : criteria.getEvidenceType().name()
                )
                .evidencePublicId(
                        evidence == null
                                ? null
                                : evidence.getPublicId()
                )
                .evidenceUrl(
                        evidence == null
                                ? null
                                : evidence.getEvidenceUrl()
                )
                .evidenceStatus(
                        evidence == null
                                ? null
                                : evidence.getEvidenceStatus().name()
                )
                .reviewerComment(
                        evidence == null
                                ? null
                                : evidence.getReviewerComment()
                )
                .subCriteriaList(new ArrayList<>())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CriteriaTemplateResponse> getCriteriaTemplates() {
        return criteriaTemplateRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toCriteriaTemplateResponse)
                .toList();
    }

    @Transactional
    public CriteriaTemplateResponse createCriteriaTemplate(CriteriaTemplateRequest request, Authentication authentication) {
        if (criteriaTemplateRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new IllegalArgumentException("Tiêu chí mẫu này đã tồn tại");
        }
        CriteriaTemplate template = CriteriaTemplate.builder()
                .name(request.name().trim())
                .description(request.description())
                .isMandatory(request.mandatory() == null || request.mandatory())
                .evidenceType(request.evidenceType())
                .level(request.level())
                .build();
        CriteriaTemplate saved = criteriaTemplateRepository.save(template);
        audit("CriteriaTemplate", saved.getId(), "CREATE", null, saved.getName(), authentication);
        return toCriteriaTemplateResponse(saved);
    }

    @Transactional
    public CriteriaTemplateResponse updateCriteriaTemplate(String publicId, CriteriaTemplateRequest request, Authentication authentication) {
        CriteriaTemplate template = criteriaTemplateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiêu chí mẫu"));
        
        if (criteriaTemplateRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), template.getId())) {
            throw new IllegalArgumentException("Tiêu chí mẫu với tên này đã tồn tại");
        }

        String oldValue = template.getName();
        template.setName(request.name().trim());
        template.setDescription(request.description());
        if (request.mandatory() != null) {
            template.setIsMandatory(request.mandatory());
        }
        template.setEvidenceType(request.evidenceType());
        template.setLevel(request.level());

        CriteriaTemplate saved = criteriaTemplateRepository.save(template);
        audit("CriteriaTemplate", saved.getId(), "UPDATE", oldValue, saved.getName(), authentication);
        return toCriteriaTemplateResponse(saved);
    }

    @Transactional
    public void deleteCriteriaTemplate(String publicId, Authentication authentication) {
        CriteriaTemplate template = criteriaTemplateRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiêu chí mẫu"));
        criteriaTemplateRepository.delete(template);
        audit("CriteriaTemplate", template.getId(), "DELETE", template.getName(), null, authentication);
    }

    private CriteriaTemplateResponse toCriteriaTemplateResponse(CriteriaTemplate template) {
        return new CriteriaTemplateResponse(
                template.getPublicId(),
                template.getName(),
                template.getDescription(),
                template.getIsMandatory(),
                template.getEvidenceType(),
                template.getLevel()
        );
    }
}
