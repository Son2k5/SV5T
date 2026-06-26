package com.example.SinhVien5T.campaign.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "criteria",
        indexes = {
                @Index(name = "idx_criteria_public_id", columnList = "public_id"),
                @Index(name = "idx_criteria_standard_id", columnList = "standard_id"),
                @Index(name = "idx_criteria_parent_id", columnList = "parent_id"),
                @Index(name = "idx_criteria_standard_parent", columnList = "standard_id,parent_id"),
                @Index(name = "idx_criteria_evidence_type", columnList = "evidence_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36, updatable = false)
    private String publicId;

    @Column(name = "name", nullable = false, length = 500)
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Standard standard;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Criteria parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Criteria> criteriaList = new LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "order_index", nullable = false, columnDefinition = "integer default 0")
    private Integer orderIndex = 0;

    @Builder.Default
    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Builder.Default
    @Column(name = "required_children_count")
    private Integer requiredChildrenCount = 0;

    @Column(name = "evidence_type", length = 50)
    @Enumerated(EnumType.STRING)
    private EvidenceType evidenceType;

    @PrePersist
    public void prePersist() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }

}
