package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.campaign.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminCampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByPublicId(String publicId);

    long countByCampaignStatus(CampaignStatus status);

    @Query("""
           select c
           from Campaign c
           where (:status is null or c.campaignStatus = :status)
             and (:status is not null or c.campaignStatus <> com.example.SinhVien5T.campaign.entity.CampaignStatus.ARCHIVED)
             and (:level is null or c.level = :level)
             and (:isGroup is null or (:isGroup = false AND (c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.INDIVIDUAL OR c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.BOTH)
                                     OR :isGroup = true AND (c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.GROUP OR c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.BOTH)))
           order by c.id desc
           """)
    Page<Campaign> search(@Param("status") CampaignStatus status,
                          @Param("level") Level level,
                          @Param("isGroup") Boolean isGroup,
                          Pageable pageable);
}