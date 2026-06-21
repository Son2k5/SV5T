package com.example.SinhVien5T.common.exception;

import com.example.SinhVien5T.auth.exception.AuthErrorMessages;
import com.example.SinhVien5T.auth.exception.InvalidEmailDomainException;
import com.example.SinhVien5T.auth.exception.InvalidTokenException;
import com.example.SinhVien5T.campaign.exception.DuplicateApplicationRecordException;
import com.example.SinhVien5T.campaign.exception.InvalidApplicationRecordStateException;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.exception.EmailExistException;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import com.example.SinhVien5T.user.exception.UserProfileConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandel {

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<ApiResponse> handleEmailExistException(EmailExistException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<ApiResponse> handleInvalidEmailDomainException(InvalidEmailDomainException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse> handleInvalidTokenException(InvalidTokenException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserProfileConflictException.class)
    public ResponseEntity<ApiResponse> handleUserProfileConflictException(UserProfileConflictException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(ApiResponse.error("Tai khoan hoac mat khau khong chinh xac"), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse> handleDisabledException(DisabledException ex) {
        String message = AuthErrorMessages.ACCOUNT_NOT_VERIFIED.equals(ex.getMessage())
                ? AuthErrorMessages.ACCOUNT_NOT_VERIFIED
                : AuthErrorMessages.ACCOUNT_DISABLED;

        return new ResponseEntity<>(ApiResponse.error(message), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(
                ApiResponse.error("Method " + ex.getMethod() + " is not supported for this endpoint"),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(ApiResponse.error("Request body is not valid JSON"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        String errorMessage = "Du lieu khong hop le";
        if (!errors.isEmpty()) {
            errorMessage = errors.values().iterator().next();
        }

        return new ResponseEntity<>(ApiResponse.error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Database constraint violation", ex);
        return new ResponseEntity<>(ApiResponse.error(dataIntegrityMessage(ex)), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse> handleFileStorageException(FileStorageException ex) {
        log.warn("File storage error", ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(ApiResponse.error("File upload vuot qua dung luong cho phep"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateApplicationRecordException.class)
    public ResponseEntity<ApiResponse> handleDuplicateApplicationRecordException(
            DuplicateApplicationRecordException ex
    ) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidApplicationRecordStateException.class)
    public ResponseEntity<ApiResponse> handleInvalidApplicationRecordStateException(
            InvalidApplicationRecordStateException ex
    ) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(
                ApiResponse.error("Loi he thong, vui long thu lai sau"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private String dataIntegrityMessage(DataIntegrityViolationException ex) {
        String detail = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();

        if (detail.contains("uk_application_record_user_campaign")) {
            return "Ban da dang ky ho so cho dot xet chon nay";
        }
        if (detail.contains("uk_user_detail_identity_card")) {
            return "So CCCD/CMND da ton tai";
        }
        if (detail.contains("uk_user_detail_student_code")) {
            return "Ma sinh vien da ton tai";
        }
        if (detail.contains("users.email") || detail.contains("email")) {
            return "Email da ton tai";
        }

        return "Du lieu khong hop le hoac trung voi du lieu da ton tai";
    }
}
