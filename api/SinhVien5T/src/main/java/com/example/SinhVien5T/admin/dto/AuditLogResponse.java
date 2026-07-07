package com.example.SinhVien5T.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private String entity;
    private String action;
    private String actorEmail;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}
