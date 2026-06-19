package com.example.SinhVien5T.common.service;

public record StoredAsset(
        String url,
        String publicId,
        String resourceType,
        String format,
        String originalFilename
) {
}
