package com.example.SinhVien5T.campaign.repository;

import com.example.SinhVien5T.campaign.entity.CriteriaTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CriteriaTemplateRepository extends JpaRepository<CriteriaTemplate, Long> {
    Optional<CriteriaTemplate> findByPublicId(String publicId);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<CriteriaTemplate> findAllByOrderByNameAsc();
}
