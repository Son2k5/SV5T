package com.example.SinhVien5T.user.mapper;

import com.example.SinhVien5T.user.dto.UserProfileResponse;
import com.example.SinhVien5T.user.entity.AddressType;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.entity.UserAddress;
import com.example.SinhVien5T.user.entity.UserDetail;
import org.springframework.stereotype.Component;

import static com.example.SinhVien5T.user.util.UserAddressUtils.findAddress;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(User user) {
        UserDetail detail = user.getDetail();
        UserAddress permanentAddress = findAddress(user, AddressType.PERMANENT);
        UserAddress temporaryAddress = findAddress(user, AddressType.TEMPORARY);

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .verified(user.isVerified())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .permanentAddress(toAddressResponse(permanentAddress))
                .temporaryAddress(toAddressResponse(temporaryAddress));

        if (detail != null) {
            builder.fullName(detail.getFullName())
                    .birthDay(detail.getBirthDay())
                    .gender(detail.getGender())
                    .identityCardNumber(maskIdentityCard(detail.getIdentityCardNumber()))
                    .ethnicity(detail.getEthnicity())
                    .school(detail.getSchool())
                    .major(detail.getMajor())
                    .academicYear(detail.getAcademicYear())
                    .studentCode(detail.getStudentCode())
                    .administrativeClass(detail.getAdministrativeClass())
                    .faculty(detail.getFaculty())
                    .currentPosition(detail.getCurrentPosition())
                    .contactEmail(detail.getContactEmail())
                    .phoneNumber(detail.getPhoneNumber())
                    .unionPosition(detail.getUnionPosition())
                    .politicalStatus(detail.getPoliticalStatus());
        }

        return builder.build();
    }

    private UserProfileResponse.AddressResponse toAddressResponse(UserAddress address) {
        if (address == null) {
            return null;
        }

        return UserProfileResponse.AddressResponse.builder()
                .provinceCity(address.getProvinceCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .build();
    }

    private String maskIdentityCard(String raw) {
        if (raw == null || raw.length() < 4) return raw;
        return "*".repeat(raw.length() - 4) + raw.substring(raw.length() - 4);
    }
}
