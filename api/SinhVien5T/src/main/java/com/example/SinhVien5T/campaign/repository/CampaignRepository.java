package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByPublicId(String publicId);

    @Query("""
           SELECT c FROM Campaign c
           WHERE c.level = :level
             AND (c.startDate IS NULL OR c.startDate <= :today)
             AND (c.endDate IS NULL OR c.endDate >= :today)
             AND (
               c.status IS NULL
              OR LOWER(c.status) IN ('open', 'opened', 'active', 'ongoing', 'publish', 'published', 'dang_mo', 'dang-mo', 'dangmo', 'mo')
             )
           ORDER BY c.startDate DESC, c.id DESC
           """)
    List<Campaign> findOpenCampaignsByLevel(
            @Param("level") Level level,
            @Param("today") LocalDate today
    );

    @Query("""
           SELECT c FROM Criteria c
           JOIN c.standard st
           JOIN st.campaign ca
           WHERE ca.id = :campaignId
           """)
    List<Criteria> findAllCriteria(@Param("campaignId") Long campaignId);

    @Query("""
           SELECT c FROM Criteria c
           JOIN c.standard st
           JOIN st.campaign ca
           WHERE ca.publicId = :campaignPublicId
           """)
    List<Criteria> findAllCriteriaByCampaignPublicId(
            @Param("campaignPublicId") String campaignPublicId
    );
}
