package com.example.SinhVien5T.campaign.entity;

import com.example.SinhVien5T.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Table(
        name = "evidence",
        indexes = {
                @Index(name = "idx_evidence_user_id", columnList = "user_id"),
                @Index(name = "idx_evidence_criteria_id", columnList = "criteria_id"),
                @Index(name = "idx_evidence_user_criteria", columnList = "user_id, criteria_id")
        }
)
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Criteria criteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    private Boolean status;

    @Column(name = "reviewer_comment")
    private String reviewerComment;

}
