package com.example.SinhVien5T.campaign.controller;

import com.example.SinhVien5T.campaign.dto.ApplicationRecordResponse;
import com.example.SinhVien5T.campaign.dto.CreateApplicationRecordRequest;
import com.example.SinhVien5T.campaign.dto.SaveEvidenceRequest;
import com.example.SinhVien5T.campaign.service.ApplicationRecordService;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
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
            Authentication authentication
    ) {
        ApplicationRecordResponse data = applicationRecordService.getMine(campaignPublicId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ thành công", data));
    }

    @PostMapping(value = "/campaigns/{campaignPublicId}/evidences", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> saveEvidence(
            @PathVariable String campaignPublicId,
            @Valid @RequestBody SaveEvidenceRequest request,
            Authentication authentication
    ) {
        applicationRecordService.saveEvidence(campaignPublicId, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Lưu minh chứng thành công", null));
    }

    @PostMapping(value = "/campaigns/{campaignPublicId}/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> saveEvidenceFile(
            @PathVariable String campaignPublicId,
            @RequestParam Long criteriaId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        applicationRecordService.saveEvidenceFile(campaignPublicId, criteriaId, file, authentication);
        return ResponseEntity.ok(ApiResponse.success("Lưu minh chứng thành công", null));
    }

    @PatchMapping("/campaigns/{campaignPublicId}/submit")
    public ResponseEntity<ApiResponse<ApplicationRecordResponse>> submit(
            @PathVariable String campaignPublicId,
            Authentication authentication
    ) {
        ApplicationRecordResponse data = applicationRecordService.submit(campaignPublicId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Nộp hồ sơ thành công", data));
    }
}
