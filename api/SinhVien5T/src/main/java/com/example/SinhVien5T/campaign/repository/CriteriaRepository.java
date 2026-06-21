package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Criteria;
import com.example.SinhVien5T.campaign.entity.EvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    List<Criteria> findByStandardId(Long standardId);

    List<Criteria> findByStandardIdAndParentIsNull(Long standardId);

    List<Criteria> findByStandardIdAndParentId(Long standardId, Long parentId);

    List<Criteria> findByEvidenceType(EvidenceType evidenceType);

    Optional<Criteria> findByIdAndStandardCampaignId(Long id, Long campaignId);

    @Query("""
            select c
            from Criteria c
            join fetch c.standard s
            left join fetch c.parent
            where s.campaign.id = :campaignId
            """)
    List<Criteria> findAllByCampaignIdWithStandardAndParent(@Param("campaignId") Long campaignId);
}
