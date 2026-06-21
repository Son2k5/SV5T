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

    Page<Campaign> findByCampaignStatusAndLevelAndAcademicYear(
            CampaignStatus campaignStatus,
            Level level,
            Long academicYear,
            Pageable pageable
    );

    @Query("""
           SELECT c FROM Campaign c
           WHERE c.level = :level
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

}
