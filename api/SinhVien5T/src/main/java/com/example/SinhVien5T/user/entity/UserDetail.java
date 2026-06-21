package com.example.SinhVien5T.user.entity;

import com.example.SinhVien5T.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "user_details",
        indexes = {
                @Index(name = "idx_user_details_full_name", columnList = "full_name"),
                @Index(name = "idx_user_details_faculty", columnList = "faculty"),
                @Index(name = "idx_user_details_academic_year", columnList = "academic_year"),
                @Index(name = "idx_user_details_faculty_year", columnList = "faculty, academic_year")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_detail_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_user_detail_identity_card", columnNames = "identity_card_number"),
                @UniqueConstraint(name = "uk_user_detail_student_code", columnNames = "student_code")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "birth_day", nullable = false)
    private LocalDate birthDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(name = "identity_card_number", nullable = false, length = 12)
    private String identityCardNumber;

    @Column(nullable = false, length = 50)
    private String ethnicity;

    @Column(nullable = false, length = 150)
    private String school;

    @Column(length = 150)
    private String major;

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @Column(name = "student_code", nullable = false, length = 50)
    private String studentCode;

    @Column(name = "administrative_class", nullable = false, length = 100)
    private String administrativeClass;

    @Column(nullable = false, length = 150)
    private String faculty;

    @Column(name = "current_position", nullable = false, length = 100)
    private String currentPosition;

    @Column(name = "contact_email", nullable = false, length = 100)
    private String contactEmail;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "union_position", length = 100)
    private String unionPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "political_status", nullable = false, length = 30)
    private PoliticalStatus politicalStatus;
}
