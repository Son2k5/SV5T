package com.example.SinhVien5T.campaign.controller;

import com.example.SinhVien5T.campaign.dto.ApplicationRecordResponse;
import com.example.SinhVien5T.campaign.dto.CreateApplicationRecordRequest;
import com.example.SinhVien5T.campaign.dto.SaveEvidenceRequest;
import com.example.SinhVien5T.campaign.entity.Level;
import com.example.SinhVien5T.campaign.service.ApplicationRecordService;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/application-records")
@RequiredArgsConstructor
@Tag(name = "Application records", description = "Tạo, cập nhật minh chứng và nộp hồ sơ xét chọn")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationRecordController {

    private final ApplicationRecordService applicationRecordService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationRecordResponse>> create(
            @Valid @RequestBody CreateApplicationRecordRequest request,
            Authentication authentication
    ) {
        ApplicationRecordResponse data = applicationRecordService.create(request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký hồ sơ thành công", data));
    }

    @GetMapping("/campaigns/{campaignPublicId}/me")
    public ResponseEntity<ApiResponse<ApplicationRecordResponse>> getMine(
            @PathVariable String campaignPublicId,
            @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
            @RequestParam(required = false) Level level,
            Authentication authentication
    ) {
        ApplicationRecordResponse data = applicationRecordService.getMine(campaignPublicId, isGroup, level, authentication);
        String message = data == null ? "Chưa có hồ sơ" : "Lấy hồ sơ thành công";
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    @PostMapping(value = "/campaigns/{campaignPublicId}/evidences", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> saveEvidence(
            @PathVariable String campaignPublicId,
            @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
            @Valid @RequestBody SaveEvidenceRequest request,
            Authentication authentication
    ) {
        applicationRecordService.saveEvidence(campaignPublicId, request, isGroup, authentication);
        return ResponseEntity.ok(ApiResponse.success("Lưu minh chứng thành công", null));
    }

    @PostMapping(value = "/campaigns/{campaignPublicId}/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> saveEvidenceFile(
            @PathVariable String campaignPublicId,
            @RequestParam String criteriaPublicId,
            @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        applicationRecordService.saveEvidenceFile(campaignPublicId, criteriaPublicId, file, isGroup, authentication);
        return ResponseEntity.ok(ApiResponse.success("Lưu minh chứng thành công", null));
    }

    @PatchMapping("/campaigns/{campaignPublicId}/submit")
    public ResponseEntity<ApiResponse<ApplicationRecordResponse>> submit(
            @PathVariable String campaignPublicId,
            @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
            @RequestParam(required = false) Level level,
            Authentication authentication
    ) {
        ApplicationRecordResponse data = applicationRecordService.submit(campaignPublicId, isGroup, level, authentication);
        return ResponseEntity.ok(ApiResponse.success("Nộp hồ sơ thành công", data));
    }
}
