package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Standard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandardRepository extends JpaRepository<Standard, Long> {

    List<Standard> findByCampaignId(Long campaignId);
}
