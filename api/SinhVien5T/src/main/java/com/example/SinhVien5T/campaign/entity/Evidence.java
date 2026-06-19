package com.example.SinhVien5T.campaign.entity;

import com.example.SinhVien5T.campaign.entity.ApplicationRecord;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "evidence",
        indexes = {
                @Index(name = "idx_evidence_application_record_id", columnList = "application_record_id"),
                @Index(name = "idx_evidence_criteria_id", columnList = "criteria_id"),
                @Index(name = "idx_evidence_status", columnList = "status"),
                @Index(name = "idx_evidence_application_criteria", columnList = "application_record_id,criteria_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_evidence_application_criteria",
                        columnNames = {"application_record_id", "criteria_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_record_id", nullable = false)
    private ApplicationRecord applicationRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criteria_id", nullable = false)
    private Criteria criteria;

    @Column(name = "evidence_url", nullable = false, length = 1000)
    private String evidenceUrl;

    @Column(name = "evidence_public_id", length = 255)
    private String evidencePublicId;

    @Column(name = "evidence_resource_type", length = 30)
    private String evidenceResourceType;

    @Column(name = "evidence_format", length = 30)
    private String evidenceFormat;

    @Column(name = "evidence_original_filename", length = 255)
    private String evidenceOriginalFilename;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = false;

    @Column(name = "reviewer_comment", columnDefinition = "TEXT")
    private String reviewerComment;
}
