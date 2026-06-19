package com.example.SinhVien5T.campaign.service;

import com.example.SinhVien5T.campaign.entity.ApplicationRecord;
import com.example.SinhVien5T.campaign.repository.ApplicationRecordRepository;
import com.example.SinhVien5T.campaign.dto.CampaignSummaryResponse;
import com.example.SinhVien5T.campaign.dto.CriteriaDTO;
import com.example.SinhVien5T.campaign.dto.StandardDTO;
import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Evidence;
import com.example.SinhVien5T.campaign.entity.Level;
import com.example.SinhVien5T.campaign.entity.Standard;
import com.example.SinhVien5T.campaign.repository.CampaignRepository;
import com.example.SinhVien5T.campaign.repository.EvidenceRepository;
import com.example.SinhVien5T.common.exception.ResourceNotFoundException;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

        private final CampaignRepository campaignRepository;
        private final ApplicationRecordRepository applicationRecordRepository;
        private final EvidenceRepository evidenceRepository;

        @Transactional(readOnly = true)
        public CampaignSummaryResponse getCurrentCampaign(Level level) {
                Campaign campaign = campaignRepository.findOpenCampaignsByLevel(level, LocalDate.now())
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn đang mở"));

                return toSummaryResponse(campaign);
        }

        @Transactional(readOnly = true)
        public List<StandardDTO> getCriteriaTreeByCampaignPublicId(
                String campaignPublicId,
                Authentication authentication
        ) {
                CustomUserDetails currentUser = getCurrentUser(authentication);

                Campaign campaign = campaignRepository.findByPublicId(campaignPublicId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt xét chọn"));

                Optional<ApplicationRecord> recordOptional = applicationRecordRepository
                        .findByUserIdAndCampaignId(currentUser.getId(), campaign.getId());

                Map<Long, Evidence> evidenceMap = new HashMap<>();

                if (recordOptional.isPresent()) {

                        List<Evidence> evidences =
                                evidenceRepository.findByApplicationRecordId(
                                        recordOptional.get().getId()
                                );

                        evidenceMap.putAll(
                                evidences.stream()
                                        .collect(Collectors.toMap(
                                                evidence -> evidence.getCriteria().getId(),
                                                evidence -> evidence
                                        ))
                        );
                }

                List<Criteria> criteriaList = campaignRepository.findAllCriteria(campaign.getId());

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

                List<StandardDTO> result = new ArrayList<>();

                for (Standard standard : campaign.getStandards()) {
                        StandardDTO standardDTO = StandardDTO.builder()
                                .id(standard.getId())
                                .name(standard.getName())
                                .description(standard.getDescription())
                                .criteriaDTOList(
                                        rootCriteriaMap.getOrDefault(
                                                standard.getId(),
                                                new ArrayList<>()
                                        )
                                )
                                .build();

                        result.add(standardDTO);
                }

                return result;
        }

        private CriteriaDTO convertToCriteriaDTO(
                Criteria criteria,
                Map<Long, Evidence> evidenceMap
        ) {
                Evidence evidence = evidenceMap.get(criteria.getId());

                return CriteriaDTO.builder()
                        .id(criteria.getId())
                        .name(criteria.getName())
                        .description(criteria.getDescription())
                        .isMandatory(criteria.getIsMandatory())
                        .requiredChildrenCount(criteria.getRequiredChildrenCount())
                        .evidenceType(
                                criteria.getEvidenceType() == null
                                        ? null
                                        : criteria.getEvidenceType().name()
                        )
                        .evidenceUrl(
                                evidence == null
                                        ? null
                                        : evidence.getEvidenceUrl()
                        )
                        .evidenceStatus(
                                evidence == null
                                        ? null
                                        : evidence.getStatus()
                        )
                        .reviewerComment(
                                evidence == null
                                        ? null
                                        : evidence.getReviewerComment()
                        )
                        .subCriteriaList(new ArrayList<>())
                        .build();
        }

        private CampaignSummaryResponse toSummaryResponse(Campaign campaign) {
                return CampaignSummaryResponse.builder()
                        .publicId(campaign.getPublicId())
                        .name(campaign.getName())
                        .academicYear(campaign.getAcademicYear())
                        .level(campaign.getLevel().name())
                        .status(campaign.getStatus())
                        .startDate(campaign.getStartDate())
                        .endDate(campaign.getEndDate())
                        .build();
        }

        private CustomUserDetails getCurrentUser(Authentication authentication) {
                if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
                        throw new ResourceNotFoundException("Người dùng chưa đăng nhập");
                }

                return user;
        }
}
