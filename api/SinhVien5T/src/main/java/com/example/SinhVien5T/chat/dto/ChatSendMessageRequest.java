package com.example.SinhVien5T.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatSendMessageRequest(
        @NotBlank(message = "Noi dung tin nhan khong duoc de trong")
        @Size(max = 2000, message = "Tin nhan toi da 2000 ky tu")
        String content
) {
}
