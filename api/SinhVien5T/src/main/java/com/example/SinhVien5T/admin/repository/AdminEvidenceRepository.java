package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.campaign.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface AdminEvidenceRepository extends JpaRepository<Evidence, Long> {
    @EntityGraph(attributePaths = {"criteria", "applicationRecord", "applicationRecord.user", "applicationRecord.user.detail", "applicationRecord.campaign"})
    Optional<Evidence> findById(Long id);

    @EntityGraph(attributePaths = {"criteria", "applicationRecord", "applicationRecord.user", "applicationRecord.user.detail", "applicationRecord.campaign"})
    Optional<Evidence> findByPublicId(String publicId);

    @EntityGraph(attributePaths = {"criteria"})
    List<Evidence> findByApplicationRecordId(Long applicationRecordId);

    Optional<Evidence> findByApplicationRecordIdAndCriteriaId(Long applicationRecordId, Long criteriaId);

    @EntityGraph(attributePaths = {"criteria", "applicationRecord", "applicationRecord.user", "applicationRecord.user.detail", "applicationRecord.campaign"})
    List<Evidence> findByApplicationRecordUserIdOrderByIdDesc(Long userId);

    @EntityGraph(attributePaths = {"criteria", "applicationRecord", "applicationRecord.user", "applicationRecord.user.detail", "applicationRecord.campaign"})
    Optional<Evidence> findByIdAndApplicationRecordUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"criteria", "applicationRecord", "applicationRecord.user", "applicationRecord.user.detail", "applicationRecord.campaign"})
    @Query("""
           select e
           from Evidence e
           join e.applicationRecord ar
           join ar.user u
           left join u.detail d
           join ar.campaign c
           join e.criteria cr
           where (:status is null or e.evidenceStatus = :status)
             and (:campaignId is null or c.id = :campaignId)
             and (:keyword is null
                or lower(u.email) like :keyword
                or lower(coalesce(d.fullName, '')) like :keyword
                or lower(coalesce(d.studentCode, '')) like :keyword
                or lower(coalesce(cr.name, '')) like :keyword)
           order by e.id desc
           """)
    Page<Evidence> search(@Param("status") EvidenceStatus status,
                          @Param("campaignId") Long campaignId,
                          @Param("keyword") String keyword,
                          Pageable pageable);

    long countByEvidenceStatus(EvidenceStatus status);

    long countByCriteriaId(Long criteriaId);

    long countByApplicationRecordCampaignIdAndEvidenceStatus(Long campaignId, EvidenceStatus status);

    long countByApplicationRecordCampaignPublicIdAndEvidenceStatus(
            String campaignPublicId,
            EvidenceStatus status
    );
    @Query("""
           select count(e)
           from Evidence e
           where e.evidenceStatus = :status
             and e.applicationRecord.createdAt between :from and :to
           """)
    long countByStatusAndApplicationCreatedAtBetween(@Param("status") EvidenceStatus status,
                                                     @Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);
}
