package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.campaign.entity.Criteria;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminCriteriaRepository extends JpaRepository<Criteria, Long> {
    Optional<Criteria> findByPublicId(String publicId);

    List<Criteria> findByStandardIdOrderByOrderIndexAscIdAsc(Long standardId);

    List<Criteria> findByStandardIdAndParentIsNullOrderByOrderIndexAscIdAsc(Long standardId);

    List<Criteria> findByStandardIdAndParentIdOrderByOrderIndexAscIdAsc(Long standardId, Long parentId);

    long countByParentId(Long parentId);

    long countByStandardCampaignId(Long campaignId);

    boolean existsByNameIgnoreCaseAndStandardId(String name, Long standardId);

    boolean existsByNameIgnoreCaseAndStandardIdAndIdNot(String name, Long standardId, Long id);
    long countByStandardId(Long standardId);


    @Query("""
           select c
           from Criteria c
           join fetch c.standard s
           join fetch s.campaign
           left join fetch c.parent
           where s.campaign.id = :campaignId
           """)
    List<Criteria> findAllByCampaignId(@Param("campaignId") Long campaignId);

    @Query("""
           select c
           from Criteria c
           join fetch c.standard s
           join fetch s.campaign
           left join fetch c.parent
           where s.campaign.id = :campaignId
             and s.isGroup = :isGroup
           """)
    List<Criteria> findAllByCampaignIdAndIsGroup(@Param("campaignId") Long campaignId, @Param("isGroup") Boolean isGroup);
}
