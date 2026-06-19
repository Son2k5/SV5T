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

    @NotBlank(message = "Ho va ten la bat buoc")
    @Size(max = 100, message = "Ho va ten khong duoc vuot qua 100 ky tu")
    private String fullName;

    @NotNull(message = "Ngay sinh la bat buoc")
    @Past(message = "Ngay sinh phai la ngay trong qua khu")
    private LocalDate birthDay;

    @NotNull(message = "Gioi tinh la bat buoc")
    private Gender gender;

    @NotBlank(message = "So CCCD/CMND la bat buoc")
    @Pattern(regexp = "^(\\d{9}|\\d{12})$", message = "So CCCD/CMND phai gom 9 hoac 12 chu so")
    private String identityCardNumber;

    @NotBlank(message = "Dan toc la bat buoc")
    @Size(max = 50, message = "Dan toc khong duoc vuot qua 50 ky tu")
    private String ethnicity;

    @NotBlank(message = "Truong la bat buoc")
    @Size(max = 150, message = "Truong khong duoc vuot qua 150 ky tu")
    private String school;

    @Size(max = 150, message = "Nganh hoc khong duoc vuot qua 150 ky tu")
    private String major;

    @NotNull(message = "Sinh vien nam la bat buoc")
    @Min(value = 1, message = "Sinh vien nam phai tu 1 tro len")
    @Max(value = 10, message = "Sinh vien nam khong duoc vuot qua 10")
    private Integer academicYear;

    @NotBlank(message = "Ma sinh vien la bat buoc")
    @Size(max = 50, message = "Ma sinh vien khong duoc vuot qua 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Ma sinh vien chi duoc chua chu, so, dau gach ngang, gach duoi hoac dau cham")
    private String studentCode;

    @NotBlank(message = "Lop hanh chinh la bat buoc")
    @Size(max = 100, message = "Lop hanh chinh khong duoc vuot qua 100 ky tu")
    private String administrativeClass;

    @NotBlank(message = "Khoa la bat buoc")
    @Size(max = 150, message = "Khoa khong duoc vuot qua 150 ky tu")
    private String faculty;

    @NotBlank(message = "Chuc vu hien tai la bat buoc")
    @Size(max = 100, message = "Chuc vu hien tai khong duoc vuot qua 100 ky tu")
    private String currentPosition;

    @Valid
    @NotNull(message = "Dia chi thuong tru la bat buoc")
    private AddressRequest permanentAddress;

    @Valid
    @NotNull(message = "Dia chi tam tru la bat buoc")
    private AddressRequest temporaryAddress;

    @NotBlank(message = "Email lien he la bat buoc")
    @Email(message = "Email lien he khong hop le")
    @Size(max = 100, message = "Email lien he khong duoc vuot qua 100 ky tu")
    private String contactEmail;

    @NotBlank(message = "So dien thoai la bat buoc")
    @Pattern(regexp = "^[0-9+\\-\\s]{8,20}$", message = "So dien thoai khong hop le")
    private String phoneNumber;

    @Size(max = 100, message = "Chuc vu doan hoi khong duoc vuot qua 100 ky tu")
    private String unionPosition;

    @NotNull(message = "Trang thai Dang vien/Doan vien la bat buoc")
    private PoliticalStatus politicalStatus;

    @Data
    public static class AddressRequest {
        @NotBlank(message = "Tinh/thanh pho la bat buoc")
        @Size(max = 100, message = "Tinh/thanh pho khong duoc vuot qua 100 ky tu")
        private String provinceCity;

        @NotBlank(message = "Quan/huyen la bat buoc")
        @Size(max = 100, message = "Quan/huyen khong duoc vuot qua 100 ky tu")
        private String district;

        @NotBlank(message = "Dia chi cu the la bat buoc")
        @Size(max = 255, message = "Dia chi cu the khong duoc vuot qua 255 ky tu")
        private String detailAddress;
    }
}
