package com.example.SinhVien5T.user.service;

import com.example.SinhVien5T.common.service.CloudinaryStorageService;
import com.example.SinhVien5T.common.service.StoredAsset;
import com.example.SinhVien5T.user.dto.UserProfileResponse;
import com.example.SinhVien5T.user.dto.UserProfileUpdateRequest;
import com.example.SinhVien5T.user.entity.AddressType;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import com.example.SinhVien5T.user.entity.User;
import com.example.SinhVien5T.user.entity.UserAddress;
import com.example.SinhVien5T.user.entity.UserDetail;
import com.example.SinhVien5T.user.exception.UserErrorMessages;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import com.example.SinhVien5T.user.exception.UserProfileConflictException;
import com.example.SinhVien5T.user.mapper.UserProfileMapper;
import com.example.SinhVien5T.user.repository.UserDetailRepository;
import com.example.SinhVien5T.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.example.SinhVien5T.user.util.UserAddressUtils.findAddress;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserProfileMapper userProfileMapper;
    private final CloudinaryStorageService cloudinaryStorageService;

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Authentication authentication) {
        User user = findAuthenticatedUserWithProfile(authentication);
        return userProfileMapper.toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(
            Authentication authentication,
            UserProfileUpdateRequest request
    ) {
        User user = findAuthenticatedUserWithProfile(authentication);
        log.info("Updating profile for userId={}", user.getId());

        validateUniqueProfileFields(user, request);

        UserDetail detail = ensureUserDetail(user);
        applyDetail(detail, request);
        applyAddress(user, AddressType.PERMANENT, request.getPermanentAddress());
        applyAddress(user, AddressType.TEMPORARY, request.getTemporaryAddress());

        User savedUser = userRepository.save(user);
        log.info("Updated profile for userId={}", savedUser.getId());
        return userProfileMapper.toResponse(savedUser);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserAvatar(Authentication authentication, String avatar) {
        User user = findAuthenticatedUserWithProfile(authentication);
        log.info("Updating avatar for userId={}", user.getId());

        String oldPublicId = user.getAvatarPublicId();
        String oldResourceType = user.getAvatarResourceType();
        StoredAsset avatarAsset = cloudinaryStorageService.uploadUserAvatar(avatar, user.getPublicId());
        applyAvatar(user, avatarAsset);

        User savedUser = userRepository.saveAndFlush(user);
        cloudinaryStorageService.deleteAsset(oldPublicId, oldResourceType);
        log.info("Updated avatar for userId={}", savedUser.getId());
        return userProfileMapper.toResponse(savedUser);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserAvatar(Authentication authentication, MultipartFile file) {
        User user = findAuthenticatedUserWithProfile(authentication);
        log.info("Updating avatar file for userId={}", user.getId());

        String oldPublicId = user.getAvatarPublicId();
        String oldResourceType = user.getAvatarResourceType();
        StoredAsset avatarAsset = cloudinaryStorageService.uploadUserAvatar(file, user.getPublicId());
        applyAvatar(user, avatarAsset);

        User savedUser = userRepository.saveAndFlush(user);
        cloudinaryStorageService.deleteAsset(oldPublicId, oldResourceType);
        log.info("Updated avatar file for userId={}", savedUser.getId());
        return userProfileMapper.toResponse(savedUser);
    }

    private void applyAvatar(User user, StoredAsset avatarAsset) {
        user.setAvatar(avatarAsset.url());
        user.setAvatarPublicId(avatarAsset.publicId());
        user.setAvatarResourceType(avatarAsset.resourceType());
    }

    private void validateUniqueProfileFields(User user, UserProfileUpdateRequest request) {
        String identityCardNumber = StringUtils.trimToNull(request.getIdentityCardNumber());
        String studentCode = StringUtils.trimToNull(request.getStudentCode());

        if (identityCardNumber != null
                && userDetailRepository.existsByIdentityCardNumberAndUserIdNot(identityCardNumber, user.getId())) {
            log.warn("Profile conflict: identityCardNumber exists for userId={}", user.getId());
            throw new UserProfileConflictException(UserErrorMessages.IDENTITY_CARD_EXISTS);
        }

        if (studentCode != null
                && userDetailRepository.existsByStudentCodeAndUserIdNot(studentCode, user.getId())) {
            log.warn("Profile conflict: studentCode exists for userId={}", user.getId());
            throw new UserProfileConflictException(UserErrorMessages.STUDENT_CODE_EXISTS);
        }
    }

    private UserDetail ensureUserDetail(User user) {
        if (user.getDetail() != null) {
            return user.getDetail();
        }

        UserDetail detail = new UserDetail();
        user.setDetail(detail);
        return detail;
    }

    private void applyDetail(UserDetail detail, UserProfileUpdateRequest request) {
        detail.setFullName(StringUtils.trimToNull(request.getFullName()));
        detail.setBirthDay(request.getBirthDay());
        detail.setGender(request.getGender());
        detail.setIdentityCardNumber(StringUtils.trimToNull(request.getIdentityCardNumber()));
        detail.setEthnicity(StringUtils.trimToNull(request.getEthnicity()));
        detail.setSchool(StringUtils.trimToNull(request.getSchool()));
        detail.setMajor(StringUtils.trimToNull(request.getMajor()));
        detail.setAcademicYear(request.getAcademicYear());
        detail.setStudentCode(StringUtils.trimToNull(request.getStudentCode()));
        detail.setAdministrativeClass(StringUtils.trimToNull(request.getAdministrativeClass()));
        detail.setFaculty(StringUtils.trimToNull(request.getFaculty()));
        detail.setCurrentPosition(StringUtils.trimToNull(request.getCurrentPosition()));
        detail.setContactEmail(StringUtils.trimToNull(request.getContactEmail()));
        detail.setPhoneNumber(StringUtils.trimToNull(request.getPhoneNumber()));
        detail.setUnionPosition(StringUtils.trimToNull(request.getUnionPosition()));
        detail.setPoliticalStatus(request.getPoliticalStatus());
    }

    private void applyAddress(
            User user,
            AddressType addressType,
            UserProfileUpdateRequest.AddressRequest request
    ) {
        if (request == null) {
            return;
        }

        UserAddress address = findAddress(user, addressType);
        if (address == null) {
            address = UserAddress.builder()
                    .addressType(addressType)
                    .build();
            user.addAddress(address);
        }

        address.setProvinceCity(StringUtils.trimToNull(request.getProvinceCity()));
        address.setDistrict(StringUtils.trimToNull(request.getDistrict()));
        address.setDetailAddress(StringUtils.trimToNull(request.getDetailAddress()));
    }

    private User findAuthenticatedUserWithProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException(UserErrorMessages.USER_NOT_AUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            if (customUserDetails.getId() == null) {
                throw new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND);
            }

            return userRepository.findByIdWithDetailAndAddresses(customUserDetails.getId())
                    .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));
        }

        return userRepository.findByEmailWithDetailAndAddresses(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(UserErrorMessages.USER_NOT_FOUND));
    }

}
