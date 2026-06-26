package com.example.SinhVien5T.admin.dto;

import com.example.SinhVien5T.user.entity.Gender;
import com.example.SinhVien5T.user.entity.PoliticalStatus;
import com.example.SinhVien5T.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminUserUpdateRequest(
        @Email(message = "Email không hợp lệ") @Size(max = 100, message = "Email không được vượt quá 100 ký tự") String email,
        Role role,
        Boolean active,
        @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự") String fullName,
        @Size(max = 50, message = "Mã sinh viên không được vượt quá 50 ký tự") String studentCode,
        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự") String phoneNumber,
        LocalDate birthDay,
        Gender gender,
        @Size(max = 12, message = "Số CCCD/CMND không được vượt quá 12 ký tự") String identityCardNumber,
        @Size(max = 50, message = "Dân tộc không được vượt quá 50 ký tự") String ethnicity,
        @Size(max = 150, message = "Trường không được vượt quá 150 ký tự") String school,
        @Size(max = 150, message = "Ngành học không được vượt quá 150 ký tự") String major,
        Integer academicYear,
        @Size(max = 100, message = "Lớp hành chính không được vượt quá 100 ký tự") String administrativeClass,
        @Size(max = 150, message = "Khoa không được vượt quá 150 ký tự") String faculty,
        @Size(max = 100, message = "Chức vụ hiện tại không được vượt quá 100 ký tự") String currentPosition,
        @Email(message = "Email liên hệ không hợp lệ") @Size(max = 100, message = "Email liên hệ không được vượt quá 100 ký tự") String contactEmail,
        @Size(max = 100, message = "Chức vụ đoàn hội không được vượt quá 100 ký tự") String unionPosition,
        PoliticalStatus politicalStatus
) {}
