package com.example.SinhVien5T.chat.dto;

import com.example.SinhVien5T.user.entity.Role;

import java.time.LocalDateTime;

public record ChatMessageDto(
        String publicId,
        String roomPublicId,
        String senderPublicId,
        String senderName,
        String senderEmail,
        Role senderRole,
        boolean mine,
        String content,
        LocalDateTime createdAt
) {
}
