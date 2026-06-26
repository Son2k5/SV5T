package com.example.SinhVien5T.campaign.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "standard",
        indexes = {
                @Index(name = "idx_standard_public_id", columnList = "public_id"),
                @Index(name = "idx_standard_campaign_id", columnList = "campaign_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Standard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36, updatable = false)
    private String publicId;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    @JsonIgnore // tránh vòng lặp vô tăng khi map Object -> JSON
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Campaign campaign;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_group", nullable = false)
    private Boolean isGroup = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 30)
    private Level level;

    @OneToMany(mappedBy = "standard")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @SQLRestriction("parent_id IS NULL")
    private Set<Criteria> criteriaList = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }

}
