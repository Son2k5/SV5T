package com.example.SinhVien5T.common.exception;

import com.example.SinhVien5T.auth.exception.InvalidTokenException;
import com.example.SinhVien5T.campaign.exception.DuplicateApplicationRecordException;
import com.example.SinhVien5T.campaign.exception.InvalidApplicationRecordStateException;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.exception.EmailExistException;
import com.example.SinhVien5T.auth.exception.InvalidEmailDomainException;
import com.example.SinhVien5T.user.exception.UserNotFoundException;
import com.example.SinhVien5T.user.exception.UserProfileConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandel {

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<ApiResponse> handleEmailExistException(EmailExistException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    // ĐÃ SỬA: Đổi tham số từ EmailExistException thành InvalidEmailDomainException
    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<ApiResponse> handleInvalidEmailDomainException(InvalidEmailDomainException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse> handleInvalidTokenException(InvalidTokenException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(UserNotFoundException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserProfileConflictException.class)
    public ResponseEntity<ApiResponse> handleUserProfileConflictException(UserProfileConflictException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    // ĐÃ XÓA: Phương thức handleInvalidTokenException bị trùng lặp ở đây

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex){
        return new ResponseEntity<>(ApiResponse.error("Tài khoản hoặc mật khẩu không chính xác"), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex){
        return new ResponseEntity<>(ApiResponse.error("Method " + ex.getMethod() + " is not supported for this endpoint"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        return new ResponseEntity<>(ApiResponse.error("Request body is not valid JSON"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex){
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        String errorMessage = "Dữ liệu không hợp lệ";
        if(!errors.isEmpty()){
            errorMessage = errors.values().iterator().next();
        }

        return new ResponseEntity<>(ApiResponse.error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Database constraint violation", ex);
        return new ResponseEntity<>(
                ApiResponse.error("Dữ liệu không hợp lệ hoặc vượt quá giới hạn cho phép"),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse> handleFileStorageException(FileStorageException ex) {
        log.warn("File storage error", ex);
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(ApiResponse.error("File upload vượt quá dung lượng cho phép"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex){
        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(
                ApiResponse.error("Lỗi hệ thống, vui lòng thử lại sau"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
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

}
