package com.example.SinhVien5T.user.dto;

import com.example.SinhVien5T.user.entity.Gender;
import com.example.SinhVien5T.user.entity.PoliticalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {

    @NotBlank(message = "Họ và tên là bắt buộc")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotNull(message = "Ngày sinh là bắt buộc")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate birthDay;

    @NotNull(message = "Giới tính là bắt buộc")
    private Gender gender;

    @NotBlank(message = "Số CCCD/CMND là bắt buộc")
    @Pattern(regexp = "^(\\d{9}|\\d{12}|\\*{5}\\d{4}|\\*{8}\\d{4})$", message = "Số CCCD/CMND không hợp lệ")
    private String identityCardNumber;

    @NotBlank(message = "Dân tộc là bắt buộc")
    @Size(max = 50, message = "Dân tộc không được vượt quá 50 ký tự")
    private String ethnicity;

    @NotBlank(message = "Trường là bắt buộc")
    @Size(max = 150, message = "Trường không được vượt quá 150 ký tự")
    private String school;

    @Size(max = 150, message = "Ngành học không được vượt quá 150 ký tự")
    private String major;

    @NotNull(message = "Sinh viên năm là bắt buộc")
    @Min(value = 1, message = "Sinh viên năm phải từ 1 trở lên")
    @Max(value = 10, message = "Sinh viên năm không được vượt quá 10")
    private Integer academicYear;

    @NotBlank(message = "Mã sinh viên là bắt buộc")
    @Size(max = 50, message = "Mã sinh viên không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Mã sinh viên chỉ được chứa chữ, số, dấu gạch ngang, gạch dưới hoặc dấu chấm")
    private String studentCode;

    @NotBlank(message = "Lớp hành chính là bắt buộc")
    @Size(max = 100, message = "Lớp hành chính không được vượt quá 100 ký tự")
    private String administrativeClass;

    @NotBlank(message = "Khoa là bắt buộc")
    @Size(max = 150, message = "Khoa không được vượt quá 150 ký tự")
    private String faculty;

    @NotBlank(message = "Chức vụ hiện tại là bắt buộc")
    @Size(max = 100, message = "Chức vụ hiện tại không được vượt quá 100 ký tự")
    private String currentPosition;

    @Valid
    @NotNull(message = "Địa chỉ thường trú là bắt buộc")
    private AddressRequest permanentAddress;

    @Valid
    @NotNull(message = "Địa chỉ tạm trú là bắt buộc")
    private AddressRequest temporaryAddress;

    @NotBlank(message = "Email liên hệ là bắt buộc")
    @Email(message = "Email liên hệ không hợp lệ")
    @Size(max = 100, message = "Email liên hệ không được vượt quá 100 ký tự")
    private String contactEmail;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(regexp = "^[0-9+\\-\\s]{8,20}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @Size(max = 100, message = "Chức vụ đoàn hội không được vượt quá 100 ký tự")
    private String unionPosition;

    @NotNull(message = "Trạng thái Đảng viên/Đoàn viên là bắt buộc")
    private PoliticalStatus politicalStatus;

    @Data
    public static class AddressRequest {
        @NotBlank(message = "Tỉnh/thành phố là bắt buộc")
        @Size(max = 100, message = "Tỉnh/thành phố không được vượt quá 100 ký tự")
        private String provinceCity;

        @NotBlank(message = "Quận/huyện là bắt buộc")
        @Size(max = 100, message = "Quận/huyện không được vượt quá 100 ký tự")
        private String district;

        @NotBlank(message = "Địa chỉ cụ thể là bắt buộc")
        @Size(max = 255, message = "Địa chỉ cụ thể không được vượt quá 255 ký tự")
        private String detailAddress;
    }
}
