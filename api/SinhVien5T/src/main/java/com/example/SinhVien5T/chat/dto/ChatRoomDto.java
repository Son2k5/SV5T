package com.example.SinhVien5T.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomDto(
        String publicId,
        String userPublicId,
        String userName,
        String userEmail,
        String userAvatar,
        String lastMessage,
        LocalDateTime lastMessageAt,
        int unreadForUser,
        int unreadForAdmin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
