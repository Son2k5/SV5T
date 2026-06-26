package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.ApplicationRecord;
import com.example.SinhVien5T.campaign.entity.ApplicationStatus;
import com.example.SinhVien5T.campaign.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {

    Optional<ApplicationRecord> findByPublicId(String publicId);

    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);
    boolean existsByUserIdAndCampaignIdAndIsGroupAndLevel(Long userId, Long campaignId, Boolean isGroup, Level level);

    @EntityGraph(attributePaths = "campaign")
    Optional<ApplicationRecord> findByUserIdAndCampaignId(Long userId, Long campaignId);

    @EntityGraph(attributePaths = "campaign")
    Optional<ApplicationRecord> findByUserIdAndCampaignIdAndIsGroupAndLevel(Long userId, Long campaignId, Boolean isGroup, Level level);

    @EntityGraph(attributePaths = "campaign")
    Optional<ApplicationRecord> findByUserIdAndCampaignPublicId(Long userId, String campaignPublicId);

    @EntityGraph(attributePaths = "campaign")
    Optional<ApplicationRecord> findByUserIdAndCampaignPublicIdAndIsGroupAndLevel(Long userId, String campaignPublicId, Boolean isGroup, Level level);

    @EntityGraph(attributePaths = {"user", "campaign"})
    Page<ApplicationRecord> findByCampaignIdAndStatus(
            Long campaignId,
            ApplicationStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "campaign"})
    Page<ApplicationRecord> findByCampaignIdAndStatusOrderByCreatedAtDesc(
            Long campaignId,
            ApplicationStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "campaign"})
    List<ApplicationRecord> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    long countByCampaignIdAndStatus(Long campaignId, ApplicationStatus status);
}
