package com.example.SinhVien5T.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAvatarUpdateRequest {

    @NotBlank(message = "Ảnh đại diện là bắt buộc")
    @Size(max = 1_500_000, message = "Ảnh đại diện không được vượt quá 1.5MB")
    @Pattern(
            regexp = "^data:image/(png|jpeg|jpg|webp);base64,[A-Za-z0-9+/=]+$",
            message = "Ảnh đại diện không hợp lệ"
    )
    private String avatar;
}
