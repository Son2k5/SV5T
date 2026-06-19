package com.example.SinhVien5T.campaign.service;

import com.example.SinhVien5T.campaign.dto.ApplicationRecordResponse;
import com.example.SinhVien5T.campaign.dto.CreateApplicationRecordRequest;
import com.example.SinhVien5T.campaign.dto.SaveEvidenceRequest;
import com.example.SinhVien5T.campaign.entity.ApplicationRecord;
import com.example.SinhVien5T.campaign.entity.ApplicationStatus;
import com.example.SinhVien5T.campaign.exception.DuplicateApplicationRecordException;
import com.example.SinhVien5T.campaign.exception.InvalidApplicationRecordStateException;
import com.example.SinhVien5T.campaign.repository.ApplicationRecordRepository;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Evidence;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.repository.EvidenceRepository;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.common.service.CloudinaryStorageService;
import com.example.SinhVien5T.common.service.StoredAsset;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ApplicationRecordService {

    private final ApplicationRecordRepository applicationRecordRepository;
    private final CampaignRepository campaignRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    @Transactional
    public ApplicationRecordResponse create(
            CreateApplicationRecordRequest request,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Campaign campaign = campaignRepository.findByPublicId(request.getCampaignPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));

        if (applicationRecordRepository.existsByUserIdAndCampaignId(
                currentUser.getId(),
                campaign.getId()
        )) {
            throw new DuplicateApplicationRecordException("Bạn đã đăng ký hồ sơ cho đợt xét chọn này");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        ApplicationRecord record = ApplicationRecord.builder()
                .user(user)
                .campaign(campaign)
                .status(ApplicationStatus.DRAFT)
                .note(request.getNote())
                .build();

        return toResponse(applicationRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public ApplicationRecordResponse getMine(
            String campaignPublicId,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        ApplicationRecord record = applicationRecordRepository
                .findByUserIdAndCampaignPublicId(currentUser.getId(), campaignPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));

        return toResponse(record);
    }

    @Transactional
    public void saveEvidence(
            String campaignPublicId,
            SaveEvidenceRequest request,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Campaign campaign = campaignRepository.findByPublicId(campaignPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));

        ApplicationRecord record = applicationRecordRepository
                .findByUserIdAndCampaignId(currentUser.getId(), campaign.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký hồ sơ cho đợt xét chọn này"));

        if (record.getStatus() != ApplicationStatus.DRAFT) {
            throw new InvalidApplicationRecordStateException("Hồ sơ đã nộp, không thể sửa minh chứng");
        }

        Criteria criteria = campaignRepository.findAllCriteria(campaign.getId())
                .stream()
                .filter(item -> item.getId().equals(request.getCriteriaId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Tiêu chí không thuộc đợt xét chọn này"));

        Evidence evidence = evidenceRepository
                .findByApplicationRecordIdAndCriteriaId(record.getId(), request.getCriteriaId())
                .orElseGet(Evidence::new);

        evidence.setApplicationRecord(record);
        evidence.setCriteria(criteria);
        String oldPublicId = evidence.getEvidencePublicId();
        String oldResourceType = evidence.getEvidenceResourceType();
        String evidenceUrl = request.getEvidenceUrl().trim();
        if (cloudinaryStorageService.isImageDataUrl(evidenceUrl)) {
            StoredAsset storedAsset = cloudinaryStorageService.uploadEvidenceDataUrl(
                    evidenceUrl,
                    campaign.getPublicId(),
                    currentUser.getPublicId(),
                    criteria.getId()
            );
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

        evidence.setStatus(false);
        evidence.setReviewerComment(null);

        evidenceRepository.saveAndFlush(evidence);
        cloudinaryStorageService.deleteAsset(oldPublicId, oldResourceType);
    }

    @Transactional
    public void saveEvidenceFile(
            String campaignPublicId,
            Long criteriaId,
            MultipartFile file,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        Campaign campaign = campaignRepository.findByPublicId(campaignPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));

        ApplicationRecord record = applicationRecordRepository
                .findByUserIdAndCampaignId(currentUser.getId(), campaign.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký hồ sơ cho đợt xét chọn này"));

        if (record.getStatus() != ApplicationStatus.DRAFT) {
            throw new InvalidApplicationRecordStateException("Hồ sơ đã nộp, không thể sửa minh chứng");
        }

        Criteria criteria = campaignRepository.findAllCriteria(campaign.getId())
                .stream()
                .filter(item -> item.getId().equals(criteriaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Tiêu chí không thuộc đợt xét chọn này"));

        Evidence evidence = evidenceRepository
                .findByApplicationRecordIdAndCriteriaId(record.getId(), criteriaId)
                .orElseGet(Evidence::new);

        String oldPublicId = evidence.getEvidencePublicId();
        String oldResourceType = evidence.getEvidenceResourceType();

        StoredAsset storedAsset = cloudinaryStorageService.uploadEvidenceFile(
                file,
                campaign.getPublicId(),
                currentUser.getPublicId(),
                criteria.getId()
        );

        evidence.setApplicationRecord(record);
        evidence.setCriteria(criteria);
        applyEvidenceAsset(evidence, storedAsset);
        evidence.setStatus(false);
        evidence.setReviewerComment(null);

        evidenceRepository.saveAndFlush(evidence);
        cloudinaryStorageService.deleteAsset(oldPublicId, oldResourceType);
    }

    @Transactional
    public ApplicationRecordResponse submit(
            String campaignPublicId,
            Authentication authentication
    ) {
        CustomUserDetails currentUser = getCurrentUser(authentication);

        ApplicationRecord record = applicationRecordRepository
                .findByUserIdAndCampaignPublicId(currentUser.getId(), campaignPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));

        if (record.getStatus() != ApplicationStatus.DRAFT) {
            throw new InvalidApplicationRecordStateException("Chỉ hồ sơ nháp mới được nộp");
        }

        record.setStatus(ApplicationStatus.SUBMITTED);

        return toResponse(applicationRecordRepository.save(record));
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
                .level(record.getCampaign().getLevel().name())
                .status(record.getStatus().name())
                .note(record.getNote())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private void applyEvidenceAsset(Evidence evidence, StoredAsset storedAsset) {
        evidence.setEvidenceUrl(storedAsset.url());
        evidence.setEvidencePublicId(storedAsset.publicId());
        evidence.setEvidenceResourceType(storedAsset.resourceType());
        evidence.setEvidenceFormat(storedAsset.format());
        evidence.setEvidenceOriginalFilename(storedAsset.originalFilename());
    }
}
