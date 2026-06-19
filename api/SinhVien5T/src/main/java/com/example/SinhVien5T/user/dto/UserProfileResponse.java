package com.example.SinhVien5T.user.dto;

import com.example.SinhVien5T.user.entity.Gender;
import com.example.SinhVien5T.user.entity.PoliticalStatus;
import com.example.SinhVien5T.user.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private String publicId;
    private String email;
    private Role role;
    private String avatar;
    private String fullName;
    private LocalDate birthDay;
    private Gender gender;
    private String identityCardNumber;
    private String ethnicity;
    private String school;
    private String major;
    private Integer academicYear;
    private String studentCode;
    private String administrativeClass;
    private String faculty;
    private String currentPosition;
    private AddressResponse permanentAddress;
    private AddressResponse temporaryAddress;
    private String contactEmail;
    private String phoneNumber;
    private String unionPosition;
    private PoliticalStatus politicalStatus;
    private boolean verified;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class AddressResponse {
        private String provinceCity;
        private String district;
        private String detailAddress;
    }
}
