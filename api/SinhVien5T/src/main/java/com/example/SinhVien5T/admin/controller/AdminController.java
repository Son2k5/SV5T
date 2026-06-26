package com.example.SinhVien5T.admin.controller;

import com.example.SinhVien5T.admin.dto.AdminUserCreateRequest;
import com.example.SinhVien5T.admin.dto.AdminUserUpdateRequest;
import com.example.SinhVien5T.admin.dto.CampaignRequest;
import com.example.SinhVien5T.admin.dto.CampaignResponse;
import com.example.SinhVien5T.admin.dto.CampaignStatsResponse;
import com.example.SinhVien5T.admin.dto.CampaignStatusRequest;
import com.example.SinhVien5T.admin.dto.CriteriaRequest;
import com.example.SinhVien5T.admin.dto.CriteriaRequirementRequest;
import com.example.SinhVien5T.admin.dto.CriteriaResponse;
import com.example.SinhVien5T.admin.dto.DashboardResponse;
import com.example.SinhVien5T.admin.dto.EvidenceResponse;
import com.example.SinhVien5T.admin.dto.IdsRequest;
import com.example.SinhVien5T.admin.dto.PageResponse;
import com.example.SinhVien5T.admin.dto.RejectRequest;
import com.example.SinhVien5T.admin.dto.ResultDetailResponse;
import com.example.SinhVien5T.admin.dto.ResultResponse;
import com.example.SinhVien5T.admin.dto.SettingRequest;
import com.example.SinhVien5T.admin.dto.SettingResponse;
import com.example.SinhVien5T.admin.dto.StandardRequest;
import com.example.SinhVien5T.admin.dto.StandardResponse;
import com.example.SinhVien5T.admin.dto.UpdatePermissionRequest;
import com.example.SinhVien5T.admin.dto.UpdateRoleRequest;
import com.example.SinhVien5T.admin.dto.UpdateStatusRequest;
import com.example.SinhVien5T.admin.dto.UserAdminResponse;
import com.example.SinhVien5T.admin.service.AdminService;
import com.example.SinhVien5T.campaign.dto.SaveEvidenceRequest;
import com.example.SinhVien5T.campaign.dto.StandardDTO;
import com.example.SinhVien5T.campaign.entity.CampaignStatus;
import com.example.SinhVien5T.campaign.entity.EvidenceStatus;
import com.example.SinhVien5T.campaign.entity.Level;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.common.entity.AuditLog;
import com.example.SinhVien5T.common.entity.Permission;
import com.example.SinhVien5T.user.entity.Role;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Quản trị người dùng, chiến dịch, tiêu chí, minh chứng và hệ thống")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<PageResponse<UserAdminResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active
    ) {
        return ok("Lấy danh sách người dùng thành công", adminService.getUsers(page, size, keyword, role, active));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userPublicId}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUser(@PathVariable String userPublicId) {
        return ok("Lấy thông tin người dùng thành công", adminService.getUser(userPublicId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users")
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(
            @Valid @RequestBody AdminUserCreateRequest request,
            Authentication authentication
    ) {
        return ok("Tạo người dùng thành công", adminService.createUser(request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{userPublicId}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(
            @PathVariable String userPublicId,
            @Valid @RequestBody AdminUserUpdateRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật người dùng thành công", adminService.updateUser(userPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{userPublicId}/role")
    public ResponseEntity<ApiResponse<UserAdminResponse>> changeRole(
            @PathVariable String userPublicId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật vai trò thành công", adminService.changeRole(userPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{userPublicId}/status")
    public ResponseEntity<ApiResponse<UserAdminResponse>> changeStatus(
            @PathVariable String userPublicId,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật trạng thái người dùng thành công", adminService.changeStatus(userPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/users/{userPublicId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String userPublicId,
            Authentication authentication
    ) {
        adminService.deleteUser(userPublicId, authentication);
        return ok("Xóa người dùng thành công", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userPublicId}/history")
    public ResponseEntity<ApiResponse<PageResponse<AuditLog>>> getUserHistory(
            @PathVariable String userPublicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ok("Lấy lịch sử người dùng thành công", adminService.getUserHistory(userPublicId, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/campaigns")
    public ResponseEntity<ApiResponse<PageResponse<CampaignResponse>>> getCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CampaignStatus status,
            @RequestParam(required = false) Level level,
            @RequestParam(required = false) Boolean isGroup
    ) {
        return ok("Lấy danh sách đợt xét chọn thành công", adminService.getCampaigns(page, size, status, level, isGroup));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/campaigns/{campaignPublicId}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaign(@PathVariable String campaignPublicId) {
        return ok("Lấy thông tin đợt xét chọn thành công", adminService.getCampaign(campaignPublicId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/campaigns")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @Valid @RequestBody CampaignRequest request,
            Authentication authentication
    ) {
        return ok("Tạo đợt xét chọn thành công", adminService.createCampaign(request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/campaigns/{campaignPublicId}")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(
            @PathVariable String campaignPublicId,
            @Valid @RequestBody CampaignRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật đợt xét chọn thành công", adminService.updateCampaign(campaignPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/campaigns/{campaignPublicId}/status")
    public ResponseEntity<ApiResponse<CampaignResponse>> changeCampaignStatus(
            @PathVariable String campaignPublicId,
            @Valid @RequestBody CampaignStatusRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật trạng thái đợt xét chọn thành công", adminService.changeCampaignStatus(campaignPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/campaigns/{campaignPublicId}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(
            @PathVariable String campaignPublicId,
            Authentication authentication
    ) {
        adminService.deleteCampaign(campaignPublicId, authentication);
        return ok("Xóa đợt xét chọn thành công", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/campaigns/{campaignPublicId}/stats")
    public ResponseEntity<ApiResponse<CampaignStatsResponse>> getCampaignStats(@PathVariable String campaignPublicId) {
        return ok("Lấy thống kê đợt xét chọn thành công", adminService.getCampaignStats(campaignPublicId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/campaigns/{campaignPublicId}/standards")
    public ResponseEntity<ApiResponse<List<StandardResponse>>> getStandards(
            @PathVariable String campaignPublicId,
            @RequestParam(required = false) Boolean isGroup
    ) {
        return ok("Lấy danh sách tiêu chuẩn thành công", adminService.getStandards(campaignPublicId, isGroup));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/campaigns/{campaignPublicId}/standards")
    public ResponseEntity<ApiResponse<StandardResponse>> createStandard(
            @PathVariable String campaignPublicId,
            @Valid @RequestBody StandardRequest request,
            Authentication authentication
    ) {
        return ok("Tạo tiêu chuẩn thành công", adminService.createStandard(campaignPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/standards/{standardPublicId}")
    public ResponseEntity<ApiResponse<StandardResponse>> updateStandard(
            @PathVariable String standardPublicId,
            @Valid @RequestBody StandardRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật tiêu chuẩn thành công", adminService.updateStandard(standardPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/standards/{standardPublicId}")
    public ResponseEntity<ApiResponse<Void>> deleteStandard(
            @PathVariable String standardPublicId,
            Authentication authentication
    ) {
        adminService.deleteStandard(standardPublicId, authentication);
        return ok("Xóa tiêu chuẩn thành công", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/standards/{standardPublicId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderStandard(
            @PathVariable String standardPublicId,
            @RequestParam Integer orderIndex
    ) {
        adminService.reorderStandard(standardPublicId, orderIndex);
        return ok("Sắp xếp tiêu chuẩn thành công", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/standards/{standardPublicId}/criteria")
    public ResponseEntity<ApiResponse<List<CriteriaResponse>>> getCriteria(@PathVariable String standardPublicId) {
        return ok("Lấy danh sách tiêu chí thành công", adminService.getCriteria(standardPublicId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/standards/{standardPublicId}/criteria")
    public ResponseEntity<ApiResponse<CriteriaResponse>> createCriteria(
            @PathVariable String standardPublicId,
            @Valid @RequestBody CriteriaRequest request,
            Authentication authentication
    ) {
        return ok("Tạo tiêu chí thành công", adminService.createCriteria(standardPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/criteria/{criteriaPublicId}")
    public ResponseEntity<ApiResponse<CriteriaResponse>> updateCriteria(
            @PathVariable String criteriaPublicId,
            @Valid @RequestBody CriteriaRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật tiêu chí thành công", adminService.updateCriteria(criteriaPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/criteria/{criteriaPublicId}/requirement")
    public ResponseEntity<ApiResponse<CriteriaResponse>> updateCriteriaRequirement(
            @PathVariable String criteriaPublicId,
            @Valid @RequestBody CriteriaRequirementRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật yêu cầu tiêu chí thành công", adminService.updateCriteriaRequirement(criteriaPublicId, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/criteria/{criteriaPublicId}")
    public ResponseEntity<ApiResponse<Void>> deleteCriteria(
            @PathVariable String criteriaPublicId,
            Authentication authentication
    ) {
        adminService.deleteCriteria(criteriaPublicId, authentication);
        return ok("Xóa tiêu chí thành công", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/criteria/{criteriaPublicId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderCriteria(
            @PathVariable String criteriaPublicId,
            @RequestParam Integer orderIndex
    ) {
        adminService.reorderCriteria(criteriaPublicId, orderIndex);
        return ok("Sắp xếp tiêu chí thành công", null);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/evidences")
    public ResponseEntity<ApiResponse<EvidenceResponse>> submitEvidence(
            @Valid @RequestBody SaveEvidenceRequest request,
            Authentication authentication
    ) {
        return ok("Nộp minh chứng thành công", adminService.submitEvidence(request, authentication));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/evidences/my")
    public ResponseEntity<ApiResponse<List<EvidenceResponse>>> getMyEvidences(Authentication authentication) {
        return ok("Lấy minh chứng của tôi thành công", adminService.getMyEvidences(authentication));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/evidences/{evidencePublicId}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> getMyEvidence(
            @PathVariable String evidencePublicId,
            Authentication authentication
    ) {
        return ok("Lấy minh chứng thành công", adminService.getMyEvidence(evidencePublicId, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @GetMapping("/admin/evidences")
    public ResponseEntity<ApiResponse<PageResponse<EvidenceResponse>>> getAdminEvidences(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EvidenceStatus status,
            @RequestParam(required = false) String campaignPublicId,
            @RequestParam(required = false) String keyword
    ) {
        return ok(
                "Lấy danh sách minh chứng thành công",
                adminService.getAdminEvidences(page, size, status, campaignPublicId, keyword)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @GetMapping("/admin/evidences/{evidencePublicId}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> getAdminEvidence(@PathVariable String evidencePublicId) {
        return ok("Lấy minh chứng thành công", adminService.getAdminEvidence(evidencePublicId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @PatchMapping("/admin/evidences/{evidencePublicId}/approve")
    public ResponseEntity<ApiResponse<EvidenceResponse>> approveEvidence(
            @PathVariable String evidencePublicId,
            @RequestParam(required = false) String comment,
            Authentication authentication
    ) {
        return ok("Duyệt đạt minh chứng thành công", adminService.approveEvidence(evidencePublicId, comment, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @PatchMapping("/admin/evidences/{evidencePublicId}/comment")
    public ResponseEntity<ApiResponse<EvidenceResponse>> updateEvidenceComment(
            @PathVariable String evidencePublicId,
            @RequestParam(required = false) String comment,
            Authentication authentication
    ) {
        return ok("Cập nhật nhận xét minh chứng thành công", adminService.updateReviewerComment(evidencePublicId, comment, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @PatchMapping("/admin/evidences/{evidencePublicId}/reject")
    public ResponseEntity<ApiResponse<EvidenceResponse>> rejectEvidence(
            @PathVariable String evidencePublicId,
            @Valid @RequestBody RejectRequest request,
            Authentication authentication
    ) {
        return ok("Từ chối minh chứng thành công", adminService.rejectEvidence(evidencePublicId, request, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @PatchMapping("/admin/evidences/bulk-approve")
    public ResponseEntity<ApiResponse<List<EvidenceResponse>>> bulkApprove(
            @Valid @RequestBody IdsRequest request,
            Authentication authentication
    ) {
        return ok("Duyệt hàng loạt thành công", adminService.bulkApprove(request, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @PatchMapping("/admin/evidences/bulk-reject")
    public ResponseEntity<ApiResponse<List<EvidenceResponse>>> bulkReject(
            @Valid @RequestBody IdsRequest request,
            @RequestParam String reason,
            Authentication authentication
    ) {
        return ok("Từ chối hàng loạt thành công", adminService.bulkReject(request, reason, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @GetMapping({"/admin/campaigns/{campaignPublicId}/results", "/admin/campaigns/results"})
    public ResponseEntity<ApiResponse<PageResponse<ResultResponse>>> getCampaignResults(
            @PathVariable(required = false) String campaignPublicId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isGroup,
            @RequestParam(required = false) Level level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ok(
                "Lấy kết quả đợt xét chọn thành công",
                adminService.getCampaignResults(campaignPublicId, status, keyword, isGroup, level, page, size)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    @GetMapping("/admin/users/{userPublicId}/campaigns/{campaignPublicId}/criteria-tree")
    public ResponseEntity<ApiResponse<List<StandardDTO>>> getUserCriteriaTree(
            @PathVariable String userPublicId,
            @PathVariable String campaignPublicId,
            @RequestParam(required = false, defaultValue = "false") Boolean isGroup,
            @RequestParam(required = false) Level level
    ) {
        return ok(
                "Lấy cây tiêu chí của sinh viên thành công",
                adminService.getUserCriteriaTree(userPublicId, campaignPublicId, isGroup, level)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR') or #userPublicId == authentication.principal.publicId")
    @GetMapping("/users/{userPublicId}/results")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getUserResults(@PathVariable String userPublicId) {
        return ok("Lấy kết quả người dùng thành công", adminService.getUserResults(userPublicId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MENTOR') or #userPublicId == authentication.principal.publicId")
    @GetMapping("/users/{userPublicId}/results/{campaignPublicId}")
    public ResponseEntity<ApiResponse<ResultDetailResponse>> getUserCampaignResult(
            @PathVariable String userPublicId,
            @PathVariable String campaignPublicId
    ) {
        return ok(
                "Lấy chi tiết kết quả thành công",
                adminService.getUserCampaignResult(userPublicId, campaignPublicId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/settings")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getSettings() {
        return ok("Lấy danh sách cấu hình thành công", adminService.getSettings());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/settings/{key}")
    public ResponseEntity<ApiResponse<SettingResponse>> getSetting(@PathVariable String key) {
        return ok("Lấy cấu hình thành công", adminService.getSetting(key));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/settings")
    public ResponseEntity<ApiResponse<SettingResponse>> createSetting(
            @Valid @RequestBody SettingRequest request,
            Authentication authentication
    ) {
        return ok("Tạo cấu hình thành công", adminService.createSetting(request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/settings/{key}")
    public ResponseEntity<ApiResponse<SettingResponse>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody SettingRequest request,
            Authentication authentication
    ) {
        return ok("Cập nhật cấu hình thành công", adminService.updateSetting(key, request, authentication));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/settings/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(
            @PathVariable String key,
            Authentication authentication
    ) {
        adminService.deleteSetting(key, authentication);
        return ok("Xóa cấu hình thành công", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ok("Lấy dữ liệu tổng quan thành công", adminService.getDashboard(from, to));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getRoles() {
        return ok("Lấy danh sách vai trò thành công", adminService.getRoles());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/permissions")
    public ResponseEntity<ApiResponse<List<Permission>>> getPermissions() {
        return ok("Lấy danh sách quyền thành công", adminService.getPermissions());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{userPublicId}/permissions")
    public ResponseEntity<ApiResponse<Void>> updateUserPermissions(
            @PathVariable String userPublicId,
            @Valid @RequestBody UpdatePermissionRequest request,
            Authentication authentication
    ) {
        adminService.updateUserPermissions(userPublicId, request, authentication);
        return ok("Cập nhật quyền thành công", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/audit-logs")
    public ResponseEntity<ApiResponse<PageResponse<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId
    ) {
        return ok("Lấy nhật ký thao tác thành công", adminService.getAuditLogs(page, size, entity, action, userId));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}
