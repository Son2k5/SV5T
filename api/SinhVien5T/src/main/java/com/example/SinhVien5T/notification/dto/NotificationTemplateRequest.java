package com.example.SinhVien5T.notification.dto;

import com.example.SinhVien5T.notification.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationTemplateRequest(
        @NotBlank(message = "Tieu de template khong duoc de trong")
        @Size(max = 255, message = "Tieu de template khong duoc vuot qua 255 ky tu")
        String subject,

        @NotBlank(message = "Noi dung template khong duoc de trong")
        String bodyTemplate,

        @NotNull(message = "Kenh thong bao khong duoc de trong")
        NotificationChannel channel,

        Boolean active
) {
}
