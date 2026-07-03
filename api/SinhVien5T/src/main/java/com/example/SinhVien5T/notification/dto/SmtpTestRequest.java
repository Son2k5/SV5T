package com.example.SinhVien5T.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SmtpTestRequest(
        String smtpHost,

        @Min(value = 1, message = "SMTP port phai lon hon 0")
        @Max(value = 65535, message = "SMTP port khong hop le")
        Integer smtpPort,

        String smtpUsername,
        String smtpPassword,

        @NotBlank(message = "Email test khong duoc de trong")
        @Email(message = "Email test khong hop le")
        String testEmail
) {
}
