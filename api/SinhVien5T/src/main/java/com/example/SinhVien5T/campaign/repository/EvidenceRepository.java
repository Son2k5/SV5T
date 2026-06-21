package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.Evidence;
import com.example.SinhVien5T.campaign.entity.EvidenceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    @Query("""
            select e
            from Evidence e
            join fetch e.criteria
            where e.applicationRecord.id = :applicationRecordId
            """)
    List<Evidence> findByApplicationRecordId(@Param("applicationRecordId") Long applicationRecordId);

    Optional<Evidence> findByApplicationRecordIdAndCriteriaId(
            Long applicationRecordId,
            Long criteriaId
    );

    @EntityGraph(attributePaths = {"criteria", "applicationRecord"})
    List<Evidence> findByApplicationRecordIdAndEvidenceStatus(
            Long applicationRecordId,
            EvidenceStatus evidenceStatus
    );

    @EntityGraph(attributePaths = {"criteria", "applicationRecord", "applicationRecord.user", "applicationRecord.campaign"})
    Page<Evidence> findByCriteriaIdAndEvidenceStatus(
            Long criteriaId,
            EvidenceStatus evidenceStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"criteria", "applicationRecord", "applicationRecord.user", "applicationRecord.campaign"})
    Page<Evidence> findByEvidenceStatus(EvidenceStatus evidenceStatus, Pageable pageable);

    long countByApplicationRecordIdAndEvidenceStatus(
            Long applicationRecordId,
            EvidenceStatus evidenceStatus
    );
}
