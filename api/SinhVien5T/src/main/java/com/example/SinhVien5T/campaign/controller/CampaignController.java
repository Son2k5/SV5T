package com.example.SinhVien5T.campaign.controller;

import com.example.SinhVien5T.campaign.dto.CampaignSummaryResponse;
import com.example.SinhVien5T.campaign.dto.ApplicationRecordResponse;
import com.example.SinhVien5T.campaign.dto.SaveEvidenceRequest;
import com.example.SinhVien5T.campaign.dto.StandardDTO;
import com.example.SinhVien5T.campaign.entity.Level;
import com.example.SinhVien5T.campaign.service.ApplicationRecordService;
import com.example.SinhVien5T.campaign.service.CampaignService;
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

import java.util.List;

@RestController
@RequestMapping("/user/campaign")
@RequiredArgsConstructor
@Tag(name = "Campaign", description = "Đợt xét chọn, tiêu chí và hồ sơ của sinh viên")
@SecurityRequirement(name = "bearerAuth")
public class CampaignController {

        private final CampaignService campaignService;
        private final ApplicationRecordService applicationRecordService;

        @GetMapping("/current")
        public ResponseEntity<ApiResponse<CampaignSummaryResponse>> getCurrentCampaign(
                        @RequestParam(defaultValue = "UNIVERSITY") Level level,
                        @RequestParam(required = false, defaultValue = "false") Boolean isGroup) {
                CampaignSummaryResponse response = campaignService.getCurrentCampaign(level, isGroup);

                return ResponseEntity.ok(
                                ApiResponse.success("Lấy đợt xét chọn đang mở thành công", response));
        }

        @GetMapping("/campaigns/{campaignPublicId}/me")
        public ResponseEntity<ApiResponse<ApplicationRecordResponse>> getMyApplication(
                        @PathVariable String campaignPublicId,
                        @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
                        @RequestParam(required = false) Level level,
                        Authentication authentication) {
                ApplicationRecordResponse response = applicationRecordService.getMine(campaignPublicId, isGroup, level,
                                authentication);

                return ResponseEntity.ok(
                                ApiResponse.success("Lấy hồ sơ thành công", response));
        }

        @PostMapping(value = "/campaigns/{campaignPublicId}/evidences", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponse<Void>> saveEvidence(
                        @PathVariable String campaignPublicId,
                        @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
                        @Valid @RequestBody SaveEvidenceRequest request,
                        Authentication authentication) {
                applicationRecordService.saveEvidence(campaignPublicId, request, isGroup, authentication);

                return ResponseEntity.ok(
                                ApiResponse.success("Lưu minh chứng thành công", null));
        }

        @PostMapping(value = "/campaigns/{campaignPublicId}/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<Void>> saveEvidenceFile(
                        @PathVariable String campaignPublicId,
                        @RequestParam("criteriaPublicId") String criteriaPublicId,
                        @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
                        @RequestPart("file") MultipartFile file,
                        Authentication authentication) {
                applicationRecordService.saveEvidenceFile(campaignPublicId, criteriaPublicId, file, isGroup, authentication);

                return ResponseEntity.ok(
                                ApiResponse.success("Lưu minh chứng thành công", null));
        }

        @PatchMapping("/campaigns/{campaignPublicId}/submit")
        public ResponseEntity<ApiResponse<ApplicationRecordResponse>> submit(
                        @PathVariable String campaignPublicId,
                        @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
                        @RequestParam(required = false) Level level,
                        Authentication authentication) {
                ApplicationRecordResponse response = applicationRecordService.submit(campaignPublicId, isGroup,
                                level, authentication);

                return ResponseEntity.ok(
                                ApiResponse.success("Nộp hồ sơ thành công", response));
        }

        @GetMapping("/{campaignPublicId}/criteria-tree")
        public ResponseEntity<ApiResponse<List<StandardDTO>>> getCriteriaTree(
                        @PathVariable String campaignPublicId,
                        @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
                        @RequestParam(required = false) Level level,
                        Authentication authentication) {

                List<StandardDTO> result = campaignService.getCriteriaTreeByCampaignPublicId(
                                campaignPublicId,
                                isGroup,
                                level,
                                authentication);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Lấy dữ liệu thành công",
                                                result));
        }
}
