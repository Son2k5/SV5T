package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    List<Evidence> findByApplicationRecordId(Long applicationRecordId);

    Optional<Evidence> findByApplicationRecordIdAndCriteriaId(
            Long applicationRecordId,
            Long criteriaId
    );
}