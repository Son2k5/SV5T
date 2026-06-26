package com.example.SinhVien5T.campaign.entity;

import com.example.SinhVien5T.campaign.entity.Campaign;
import com.example.SinhVien5T.campaign.entity.Evidence;
import com.example.SinhVien5T.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "application_record",
        indexes = {
                @Index(name = "idx_application_record_public_id", columnList = "public_id"),
                @Index(name = "idx_application_record_user_id", columnList = "user_id"),
                @Index(name = "idx_application_record_campaign_id", columnList = "campaign_id"),
                @Index(name = "idx_application_record_status", columnList = "status"),
                @Index(name = "idx_application_record_user_campaign", columnList = "user_id,campaign_id"),
                @Index(name = "idx_application_record_campaign_status", columnList = "campaign_id,status"),
                @Index(name = "idx_application_record_user_status", columnList = "user_id,status"),
                @Index(name = "idx_application_record_campaign_status_created_at", columnList = "campaign_id,status,created_at"),
                @Index(name = "idx_application_record_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_application_record_public_id",
                        columnNames = "public_id"
                ),
                @UniqueConstraint(
                        name = "uk_application_record_user_campaign_type_level",
                        columnNames = {"user_id", "campaign_id", "is_group", "level"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36, updatable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_group")
    @Builder.Default
    private Boolean isGroup = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 30)
    private Level level;

    @OneToMany(mappedBy = "applicationRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Evidence> evidences = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID().toString();
        }
    }
}
