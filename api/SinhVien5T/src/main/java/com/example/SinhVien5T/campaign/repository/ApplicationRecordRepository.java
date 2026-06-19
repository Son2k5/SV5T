package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.ApplicationRecord;
import com.example.SinhVien5T.campaign.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {

    Optional<ApplicationRecord> findByPublicId(String publicId);

    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);

    Optional<ApplicationRecord> findByUserIdAndCampaignId(Long userId, Long campaignId);

    Optional<ApplicationRecord> findByUserIdAndCampaignPublicId(Long userId, String campaignPublicId);

    List<ApplicationRecord> findByCampaignIdAndStatus(Long campaignId, ApplicationStatus status);
}