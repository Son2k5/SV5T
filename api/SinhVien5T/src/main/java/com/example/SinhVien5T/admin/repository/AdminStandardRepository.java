package com.example.SinhVien5T.admin.repository;

import com.example.SinhVien5T.campaign.entity.Standard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminStandardRepository extends JpaRepository<Standard, Long> {
    List<Standard> findByCampaignIdOrderByIdAsc(Long campaignId);

    List<Standard> findByCampaignIdAndIsGroupOrderByIdAsc(Long campaignId, Boolean isGroup);

    Optional<Standard> findByPublicId(String publicId);
}
