package com.example.SinhVien5T.auth.controller;

import com.example.SinhVien5T.auth.dto.request.UserLoginRequest;
import com.example.SinhVien5T.auth.dto.request.UserRegisterRequest;
import com.example.SinhVien5T.auth.dto.request.UserResetPwRequest;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.auth.service.AuthService;
import com.example.SinhVien5T.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập, làm mới token và đặt lại mật khẩu")
public class UserAuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRegisterRequest request) throws Exception {

        authService.register(request);

        ApiResponse apiResponse = ApiResponse.success("Đăng ký thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/verify_register_token")
    public ResponseEntity<ApiResponse> verifyRegisterToken(@RequestParam String token, HttpServletResponse response) throws IOException {
        authService.verifyRegisterToken(token, response);

        ApiResponse apiResponse = ApiResponse.success("Xác minh thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) throws MessagingException {

        Map<String, Object> data = authService.login(userLoginRequest, request, response);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Đăng nhập thành công", data);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @PostMapping("/log_out")
    public ResponseEntity<ApiResponse> logOut(HttpServletRequest request, HttpServletResponse response){
        authService.logOut(request, response);

        ApiResponse apiResponse = ApiResponse.success("Đăng xuất thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh_access_token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshAccessToken(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> body = authService.refreshAccessToken(request, response);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("Làm mới phiên đăng nhập thành công", body);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/missing_password")
    public void missingPassWord(@RequestParam String email) throws MessagingException {
        authService.missingPassWord(email);
    }

    @GetMapping("/verify_reset_token")
    public ResponseEntity<ApiResponse> checkResetToken(@RequestParam String token) throws IOException {
        // Logic: Chỉ check xem token có tồn tại và còn hạn không
        boolean isValid = authService.verifyResetPwToken(token);

        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Mã đặt lại mật khẩu hợp lệ", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Mã đặt lại mật khẩu đã hết hạn hoặc không tồn tại"));
        }
    }

    @PostMapping("/reset_password")
    public ResponseEntity<ApiResponse<String>> resetPassWord(@Valid @RequestBody UserResetPwRequest request) throws MessagingException {

        authService.resetPassWord(request.getToken(), request.getNewPw());

        ApiResponse apiResponse = ApiResponse.success("Đổi mật khẩu thành công", null);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


}


