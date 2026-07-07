package com.example.SinhVien5T.campaign.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "campaign",
        indexes = {
                @Index(name = "idx_campaign_public_id", columnList = "public_id"),
                @Index(name = "idx_campaign_status", columnList = "campaign_status"),
                @Index(name = "idx_campaign_level", columnList = "level"),
                @Index(name = "idx_campaign_academic_year", columnList = "academic_year"),
                @Index(name = "idx_campaign_status_level_year", columnList = "campaign_status, level, academic_year"),
                @Index(name = "idx_campaign_is_group", columnList = "is_group")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36, updatable = false)
    private String publicId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "academic_year", nullable = false)
    private Long academicYear;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Level level;

    @Builder.Default
    @Column(name = "campaign_status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CampaignStatus campaignStatus = CampaignStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @Column(name = "is_group", nullable = false)
    @Convert(converter = CampaignTypeConverter.class)
    private CampaignType isGroup = CampaignType.INDIVIDUAL;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDate createAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Standard> standards = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID().toString();
        }
    }
}
