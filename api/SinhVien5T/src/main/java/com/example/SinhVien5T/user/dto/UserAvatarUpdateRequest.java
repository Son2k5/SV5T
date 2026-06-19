package com.example.SinhVien5T.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAvatarUpdateRequest {

    @NotBlank(message = "Avatar la bat buoc")
    @Size(max = 1_500_000, message = "Avatar khong duoc vuot qua 1.5MB")
    @Pattern(
            regexp = "^data:image/(png|jpeg|jpg|webp);base64,[A-Za-z0-9+/=]+$",
            message = "Avatar khong hop le"
    )
    private String avatar;
}
