package com.example.SinhVien5T.chat.controller;

import com.example.SinhVien5T.admin.dto.PageResponse;
import com.example.SinhVien5T.chat.dto.ChatMessageDto;
import com.example.SinhVien5T.chat.dto.ChatRoomDto;
import com.example.SinhVien5T.chat.dto.ChatSendMessageRequest;
import com.example.SinhVien5T.chat.service.ChatService;
import com.example.SinhVien5T.common.dto.response.ApiResponse;
import com.example.SinhVien5T.user.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Realtime support chat giua user va admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/me/room")
    public ResponseEntity<ApiResponse<ChatRoomDto>> getMyRoom(Authentication authentication) {
        return ok("Lay phong chat cua toi thanh cong", chatService.getOrCreateMyRoom(currentUser(authentication)));
    }

    @GetMapping("/me/messages")
    public ResponseEntity<ApiResponse<PageResponse<ChatMessageDto>>> getMyMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Authentication authentication
    ) {
        return ok("Lay tin nhan cua toi thanh cong", chatService.getMyMessages(currentUser(authentication), page, size));
    }

    @PostMapping("/me/messages")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendMyMessage(
            @Valid @RequestBody ChatSendMessageRequest request,
            Authentication authentication
    ) {
        return ok("Gui tin nhan thanh cong", chatService.sendMyMessage(currentUser(authentication), request));
    }

    @PatchMapping("/me/read")
    public ResponseEntity<ApiResponse<ChatRoomDto>> markMyRoomRead(Authentication authentication) {
        return ok("Danh dau da doc thanh cong", chatService.markMyRoomRead(currentUser(authentication)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/rooms")
    public ResponseEntity<ApiResponse<PageResponse<ChatRoomDto>>> getAdminRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ok("Lay danh sach phong chat thanh cong", chatService.getAdminRooms(keyword, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/rooms/{roomPublicId}/messages")
    public ResponseEntity<ApiResponse<PageResponse<ChatMessageDto>>> getAdminMessages(
            @PathVariable String roomPublicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Authentication authentication
    ) {
        return ok("Lay tin nhan phong chat thanh cong", chatService.getAdminMessages(
                roomPublicId,
                currentUser(authentication),
                page,
                size
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/rooms/{roomPublicId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendAdminMessage(
            @PathVariable String roomPublicId,
            @Valid @RequestBody ChatSendMessageRequest request,
            Authentication authentication
    ) {
        return ok("Gui tin nhan thanh cong", chatService.sendAdminMessage(
                roomPublicId,
                currentUser(authentication),
                request
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/rooms/{roomPublicId}/read")
    public ResponseEntity<ApiResponse<ChatRoomDto>> markAdminRoomRead(@PathVariable String roomPublicId) {
        return ok("Danh dau da doc thanh cong", chatService.markAdminRoomRead(roomPublicId));
    }

    private CustomUserDetails currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new IllegalArgumentException("Nguoi dung chua dang nhap");
        }
        return user;
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
}
