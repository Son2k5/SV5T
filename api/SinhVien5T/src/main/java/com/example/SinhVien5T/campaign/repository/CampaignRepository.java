package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByPublicId(String publicId);

    Page<Campaign> findByCampaignStatus(CampaignStatus campaignStatus, Pageable pageable);

    Page<Campaign> findByLevel(Level level, Pageable pageable);

    Page<Campaign> findByAcademicYear(Long academicYear, Pageable pageable);

    List<Campaign> findByCampaignStatusAndEndDateBetween(
            CampaignStatus campaignStatus,
            LocalDate from,
            LocalDate to
    );

    Page<Campaign> findByCampaignStatusAndLevelAndAcademicYear(
            CampaignStatus campaignStatus,
            Level level,
            Long academicYear,
            Pageable pageable
    );

    @Query("""
           SELECT c FROM Campaign c
           WHERE (:level = com.example.SinhVien5T.campaign.entity.Level.UNIVERSITY AND c.level IN (com.example.SinhVien5T.campaign.entity.Level.UNIVERSITY, com.example.SinhVien5T.campaign.entity.Level.UNI_CITY, com.example.SinhVien5T.campaign.entity.Level.UNI_NATION, com.example.SinhVien5T.campaign.entity.Level.ALL)
              OR :level = com.example.SinhVien5T.campaign.entity.Level.CITY AND c.level IN (com.example.SinhVien5T.campaign.entity.Level.CITY, com.example.SinhVien5T.campaign.entity.Level.UNI_CITY, com.example.SinhVien5T.campaign.entity.Level.CITY_NATION, com.example.SinhVien5T.campaign.entity.Level.ALL)
              OR :level = com.example.SinhVien5T.campaign.entity.Level.NATION AND c.level IN (com.example.SinhVien5T.campaign.entity.Level.NATION, com.example.SinhVien5T.campaign.entity.Level.UNI_NATION, com.example.SinhVien5T.campaign.entity.Level.CITY_NATION, com.example.SinhVien5T.campaign.entity.Level.ALL)
              OR :level = com.example.SinhVien5T.campaign.entity.Level.ALL AND c.level = com.example.SinhVien5T.campaign.entity.Level.ALL
              OR c.level = :level)
             AND (c.startDate IS NULL OR c.startDate <= :today)
             AND (c.endDate IS NULL OR c.endDate >= :today)
             AND c.campaignStatus = com.example.SinhVien5T.campaign.entity.CampaignStatus.ACTIVE
           ORDER BY c.startDate DESC, c.id DESC
           """)
    List<Campaign> findOpenCampaignsByLevel(
            @Param("level") Level level,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    @Query("""
           SELECT c FROM Campaign c
           WHERE (:level = com.example.SinhVien5T.campaign.entity.Level.UNIVERSITY AND c.level IN (com.example.SinhVien5T.campaign.entity.Level.UNIVERSITY, com.example.SinhVien5T.campaign.entity.Level.UNI_CITY, com.example.SinhVien5T.campaign.entity.Level.UNI_NATION, com.example.SinhVien5T.campaign.entity.Level.ALL)
              OR :level = com.example.SinhVien5T.campaign.entity.Level.CITY AND c.level IN (com.example.SinhVien5T.campaign.entity.Level.CITY, com.example.SinhVien5T.campaign.entity.Level.UNI_CITY, com.example.SinhVien5T.campaign.entity.Level.CITY_NATION, com.example.SinhVien5T.campaign.entity.Level.ALL)
              OR :level = com.example.SinhVien5T.campaign.entity.Level.NATION AND c.level IN (com.example.SinhVien5T.campaign.entity.Level.NATION, com.example.SinhVien5T.campaign.entity.Level.UNI_NATION, com.example.SinhVien5T.campaign.entity.Level.CITY_NATION, com.example.SinhVien5T.campaign.entity.Level.ALL)
              OR :level = com.example.SinhVien5T.campaign.entity.Level.ALL AND c.level = com.example.SinhVien5T.campaign.entity.Level.ALL
              OR c.level = :level)
             AND (:isGroup = false AND (c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.INDIVIDUAL OR c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.BOTH)
                  OR :isGroup = true AND (c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.GROUP OR c.isGroup = com.example.SinhVien5T.campaign.entity.CampaignType.BOTH))
             AND (c.startDate IS NULL OR c.startDate <= :today)
             AND (c.endDate IS NULL OR c.endDate >= :today)
             AND c.campaignStatus = com.example.SinhVien5T.campaign.entity.CampaignStatus.ACTIVE
           ORDER BY c.startDate DESC, c.id DESC
           """)
    List<Campaign> findOpenCampaignsByLevelAndIsGroup(
            @Param("level") Level level,
            @Param("isGroup") Boolean isGroup,
            @Param("today") LocalDate today,
            Pageable pageable
    );

}
