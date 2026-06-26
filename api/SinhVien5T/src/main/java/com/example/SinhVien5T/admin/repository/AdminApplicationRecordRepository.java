package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.campaign.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface AdminApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {
    long countByCampaignId(Long campaignId);
    long countByCampaignIdAndStatus(Long campaignId, ApplicationStatus status);
    long countByStatus(ApplicationStatus status);
    long countByStatusAndCreatedAtBetween(ApplicationStatus status, LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {"campaign"})
    Optional<ApplicationRecord> findByUserIdAndCampaignId(Long userId, Long campaignId);

    @EntityGraph(attributePaths = {"campaign"})
    Optional<ApplicationRecord> findByUserIdAndCampaignIdAndIsGroup(Long userId, Long campaignId, Boolean isGroup);

    @EntityGraph(attributePaths = {"campaign"})
    List<ApplicationRecord> findByUserIdAndCampaignIdAndIsGroupOrderByCreatedAtDesc(Long userId, Long campaignId, Boolean isGroup);

    @EntityGraph(attributePaths = {"campaign"})
    Optional<ApplicationRecord> findByUserIdAndCampaignIdAndIsGroupAndLevel(Long userId, Long campaignId, Boolean isGroup, Level level);

    @EntityGraph(attributePaths = {"user", "user.detail", "campaign"})
    List<ApplicationRecord> findByUserPublicIdOrderByCreatedAtDesc(String userPublicId);

    @EntityGraph(attributePaths = {"user", "user.detail", "campaign"})
    @Query("""
           select ar
           from ApplicationRecord ar
           join ar.user u
           left join u.detail d
           join ar.campaign c
           where (:campaignPublicId is null or c.publicId = :campaignPublicId)
             and ar.status in :statuses
             and (:isGroup is null or coalesce(ar.isGroup, false) = :isGroup)
             and (:level is null or ar.level = :level)
             and (:keyword is null
                or lower(u.email) like :keyword
                or lower(coalesce(d.fullName, '')) like :keyword
                or lower(coalesce(d.studentCode, '')) like :keyword)
           order by ar.createdAt desc
           """)
    Page<ApplicationRecord> searchResults(@Param("campaignPublicId") String campaignPublicId,
                                          @Param("statuses") Collection<ApplicationStatus> statuses,
                                          @Param("keyword") String keyword,
                                          @Param("isGroup") Boolean isGroup,
                                          @Param("level") Level level,
                                          Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.detail", "campaign", "evidences", "evidences.criteria"})
    @Query("""
           select ar
           from ApplicationRecord ar
           where ar.user.publicId = :userPublicId
             and ar.campaign.publicId = :campaignPublicId
           """)
    Optional<ApplicationRecord> findResultDetail(@Param("userPublicId") String userPublicId,
                                                 @Param("campaignPublicId") String campaignPublicId);
}