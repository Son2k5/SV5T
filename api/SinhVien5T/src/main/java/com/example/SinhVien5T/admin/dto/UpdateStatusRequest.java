package com.example.SinhVien5T.admin.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest  (@NotNull(message = "Trạng thái người dùng không được để trống") Boolean active){
    
}
