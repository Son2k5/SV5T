package com.example.SinhVien5T.user.controller;

import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.dto.UserAvatarUpdateRequest;
import com.example.SinhVien5T.user.dto.UserPasswordChangeRequest;
import com.example.SinhVien5T.user.dto.UserProfileResponse;
import com.example.SinhVien5T.user.dto.UserProfileUpdateRequest;
import com.example.SinhVien5T.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User profile", description = "Thông tin và ảnh đại diện của người dùng hiện tại")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication authentication) {
        UserProfileResponse data = userService.getCurrentUserProfile(authentication);
        ApiResponse<UserProfileResponse> response = ApiResponse.success("Lấy thông tin người dùng thành công", data);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse data = userService.updateCurrentUserProfile(authentication, request);
        ApiResponse<UserProfileResponse> response = ApiResponse.success("Cập nhật thông tin người dùng thành công", data);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/profile/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody UserPasswordChangeRequest request
    ) {
        userService.changeCurrentUserPassword(authentication, request);
        ApiResponse<Void> response = ApiResponse.success("Đổi mật khẩu thành công", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/profile/avatar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatar(
            Authentication authentication,
            @Valid @RequestBody UserAvatarUpdateRequest request
    ) {
        UserProfileResponse data = userService.updateCurrentUserAvatar(authentication, request.getAvatar());
        ApiResponse<UserProfileResponse> response = ApiResponse.success("Cập nhật ảnh đại diện thành công", data);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatarFile(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        UserProfileResponse data = userService.updateCurrentUserAvatar(authentication, file);
        ApiResponse<UserProfileResponse> response = ApiResponse.success("Cập nhật ảnh đại diện thành công", data);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
