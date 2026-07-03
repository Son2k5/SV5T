package com.example.SinhVien5T.user.service;

import com.example.SinhVien5T.auth.repository.RefreshTokenRepository;
import com.example.SinhVien5T.auth.service.RedisTokenService;
import com.example.SinhVien5T.auth.service.TokenStateService;
import com.example.SinhVien5T.common.security.CachedUserPrincipalService;
import com.example.SinhVien5T.common.service.CloudinaryStorageService;
import com.example.SinhVien5T.common.service.AssetCleanupScheduler;
import com.example.SinhVien5T.common.service.StoredAsset;
import com.example.SinhVien5T.common.config.CacheConfig;
import com.example.SinhVien5T.user.dto.UserPasswordChangeRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Function;

import static com.example.SinhVien5T.user.util.UserAddressUtils.findAddress;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserProfileMapper userProfileMapper;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final AssetCleanupScheduler assetCleanupScheduler;
    private final PasswordEncoder passwordEncoder;
    private final CachedUserPrincipalService cachedUserPrincipalService;
    private final RedisTokenService redisTokenService;
    private final TokenStateService tokenStateService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.redis.token.enabled:true}")
    private boolean redisTokenEnabled;

    @Cacheable(cacheNames = CacheConfig.USER_PROFILE, key = "#authentication.principal.id", sync = true)
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Authentication authentication) {
        User user = findAuthenticatedUserWithProfile(authentication);
        return userProfileMapper.toResponse(user);
    }

    @CachePut(cacheNames = CacheConfig.USER_PROFILE, key = "#authentication.principal.id")
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

    @CachePut(cacheNames = CacheConfig.USER_PROFILE, key = "#authentication.principal.id")
    @Transactional
    public UserProfileResponse updateCurrentUserAvatar(Authentication authentication, String avatar) {
        return updateAvatarInternal(
                authentication,
                userPublicId -> cloudinaryStorageService.uploadUserAvatar(avatar, userPublicId),
                "avatar"
        );
    }

    @CachePut(cacheNames = CacheConfig.USER_PROFILE, key = "#authentication.principal.id")
    @Transactional
    public UserProfileResponse updateCurrentUserAvatar(Authentication authentication, MultipartFile file) {
        return updateAvatarInternal(
                authentication,
                userPublicId -> cloudinaryStorageService.uploadUserAvatar(file, userPublicId),
                "avatar file"
        );
    }

    @Transactional
    public void changeCurrentUserPassword(
            Authentication authentication,
            UserPasswordChangeRequest request
    ) {
        User user = findAuthenticatedUserWithProfile(authentication);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        cachedUserPrincipalService.evict(user.getId());
        revokeUserSessions(user.getId());
    }

    private UserProfileResponse updateAvatarInternal(
            Authentication authentication,
            Function<String, StoredAsset> uploader,
            String label
    ) {
        User user = findAuthenticatedUserWithProfile(authentication);
        log.info("Updating {} for userId={}", label, user.getId());

        String oldPublicId = user.getAvatarPublicId();
        String oldResourceType = user.getAvatarResourceType();
        StoredAsset avatarAsset = uploader.apply(user.getPublicId());
        assetCleanupScheduler.deleteAfterRollback(avatarAsset.publicId(), avatarAsset.resourceType());
        applyAvatar(user, avatarAsset);

        User savedUser = userRepository.saveAndFlush(user);
        assetCleanupScheduler.deleteAfterCommit(oldPublicId, oldResourceType);
        log.info("Updated {} for userId={}", label, savedUser.getId());
        return userProfileMapper.toResponse(savedUser);
    }

    private void applyAvatar(User user, StoredAsset avatarAsset) {
        user.setAvatar(avatarAsset.url());
        user.setAvatarPublicId(avatarAsset.publicId());
        user.setAvatarResourceType(avatarAsset.resourceType());
    }

    private void revokeUserSessions(Long userId) {
        if (redisTokenEnabled) {
            redisTokenService.revokeAllRefreshTokens(userId);
            tokenStateService.revokeAllAccessTokens(userId);
            return;
        }

        refreshTokenRepository.revokeAllByUserId(userId);
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
