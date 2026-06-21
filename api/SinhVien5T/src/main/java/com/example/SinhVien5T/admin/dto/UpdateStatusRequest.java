package com.example.SinhVien5T.admin.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest  (@NotNull Boolean active){
    
}
