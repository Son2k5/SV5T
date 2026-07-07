package com.example.SinhVien5T.campaign.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "criteria_template",
        indexes = {
                @Index(name = "idx_criteria_template_public_id", columnList = "public_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CriteriaTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36, updatable = false)
    private String publicId;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Column(name = "evidence_type", length = 50)
    @Enumerated(EnumType.STRING)
    private EvidenceType evidenceType;

    @Column(name = "level", length = 30)
    @Enumerated(EnumType.STRING)
    private Level level;

    @PrePersist
    public void prePersist() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }
}
