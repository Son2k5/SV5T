package com.example.SinhVien5T.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.SinhVien5T.common.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService {

    private static final Pattern IMAGE_DATA_URL_PATTERN = Pattern.compile(
            "^data:image/(png|jpeg|jpg|webp);base64,[A-Za-z0-9+/=]+$"
    );
    private static final Set<String> AVATAR_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final Set<String> EVIDENCE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "pdf", "doc", "docx");
    private static final Set<String> EVIDENCE_RAW_EXTENSIONS = Set.of("pdf", "doc", "docx");

    private final Cloudinary cloudinary;

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${app.cloudinary.root-folder:sinhvien5t}")
    private String rootFolder;

    @Value("${app.cloudinary.avatar-folder:users/avatars}")
    private String avatarFolder;

    @Value("${app.cloudinary.evidence-folder:campaign/evidences}")
    private String evidenceFolder;

    @Value("${app.cloudinary.avatar-max-bytes:1048576}")
    private long avatarMaxBytes;

    @Value("${app.cloudinary.evidence-max-bytes:5242880}")
    private long evidenceMaxBytes;

    public boolean isImageDataUrl(String value) {
        return value != null && IMAGE_DATA_URL_PATTERN.matcher(value.trim()).matches();
    }

    public StoredAsset uploadUserAvatar(String imageDataUrl, String userPublicId) {
        ensureConfigured();
        if (!isImageDataUrl(imageDataUrl)) {
            throw new IllegalArgumentException("Avatar không hợp lệ");
        }
        validateDataUrlSize(imageDataUrl, avatarMaxBytes, "Avatar không được vượt quá 1MB");

        String extension = imageDataUrlExtension(imageDataUrl);
        String folder = joinFolder(rootFolder, avatarFolder, sanitizePathSegment(userPublicId));
        return upload(imageDataUrl.trim(), folder, UUID.randomUUID().toString(), "image", extension, "avatar." + extension);
    }

    public StoredAsset uploadUserAvatar(MultipartFile file, String userPublicId) {
        ensureConfigured();
        validateMultipartFile(file, avatarMaxBytes, AVATAR_EXTENSIONS, "Avatar");

        String extension = fileExtension(file.getOriginalFilename());
        String folder = joinFolder(rootFolder, avatarFolder, sanitizePathSegment(userPublicId));
        return upload(fileBytes(file), folder, UUID.randomUUID().toString(), "image", extension, file.getOriginalFilename());
    }

    public StoredAsset uploadEvidenceDataUrl(
            String imageDataUrl,
            String campaignPublicId,
            String userPublicId,
            Long criteriaId
    ) {
        ensureConfigured();
        if (!isImageDataUrl(imageDataUrl)) {
            throw new IllegalArgumentException("Minh chứng ảnh không hợp lệ");
        }
        validateDataUrlSize(imageDataUrl, evidenceMaxBytes, "Minh chứng không được vượt quá 5MB");

        String extension = imageDataUrlExtension(imageDataUrl);
        String folder = evidenceFolder(campaignPublicId, userPublicId, criteriaId);
        return upload(imageDataUrl.trim(), folder, UUID.randomUUID().toString(), "image", extension, "evidence." + extension);
    }

    public StoredAsset uploadEvidenceFile(
            MultipartFile file,
            String campaignPublicId,
            String userPublicId,
            Long criteriaId
    ) {
        ensureConfigured();
        validateMultipartFile(file, evidenceMaxBytes, EVIDENCE_EXTENSIONS, "Minh chứng");

        String extension = fileExtension(file.getOriginalFilename());
        String resourceType = EVIDENCE_RAW_EXTENSIONS.contains(extension) ? "raw" : "image";
        String publicId = UUID.randomUUID().toString();
        if ("raw".equals(resourceType)) {
            publicId = publicId + "." + extension;
        }

        return upload(
                fileBytes(file),
                evidenceFolder(campaignPublicId, userPublicId, criteriaId),
                publicId,
                resourceType,
                extension,
                file.getOriginalFilename()
        );
    }

    public void deleteAsset(String publicId, String resourceType) {
        if (StringUtils.isBlank(publicId) || StringUtils.isBlank(resourceType)) {
            return;
        }
        ensureConfigured();

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException ex) {
            throw new FileStorageException("Không thể xóa file cũ trên Cloudinary", ex);
        }
    }

    private StoredAsset upload(
            Object file,
            String folder,
            String publicId,
            String resourceType,
            String format,
            String originalFilename
    ) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", resourceType,
                    "overwrite", false,
                    "unique_filename", false,
                    "use_filename", false
            ));

            return new StoredAsset(
                    stringValue(result.get("secure_url")),
                    stringValue(result.get("public_id")),
                    stringValue(result.get("resource_type")),
                    StringUtils.defaultIfBlank(stringValue(result.get("format")), format),
                    originalFilename
            );
        } catch (IOException | RuntimeException ex) {
            throw new FileStorageException("Không thể upload file lên Cloudinary", ex);
        }
    }

    private String evidenceFolder(String campaignPublicId, String userPublicId, Long criteriaId) {
        return joinFolder(
                rootFolder,
                evidenceFolder,
                sanitizePathSegment(campaignPublicId),
                sanitizePathSegment(userPublicId),
                criteriaId == null ? "unknown-criteria" : "criteria-" + criteriaId
        );
    }

    private void ensureConfigured() {
        if (StringUtils.isAnyBlank(cloudName, apiKey, apiSecret)) {
            throw new FileStorageException("Chưa cấu hình Cloudinary");
        }
    }

    private void validateMultipartFile(
            MultipartFile file,
            long maxBytes,
            Set<String> allowedExtensions,
            String label
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(label + " không được để trống");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(label + " vượt quá dung lượng cho phép");
        }

        String extension = fileExtension(file.getOriginalFilename());
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(label + " chỉ hỗ trợ: " + String.join(", ", allowedExtensions));
        }
    }

    private void validateDataUrlSize(String dataUrl, long maxBytes, String message) {
        Matcher matcher = IMAGE_DATA_URL_PATTERN.matcher(dataUrl.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Ảnh không hợp lệ");
        }

        int commaIndex = dataUrl.indexOf(',');
        if (commaIndex < 0) {
            throw new IllegalArgumentException("Ảnh không hợp lệ");
        }

        long estimatedBytes = (dataUrl.length() - commaIndex - 1L) * 3L / 4L;
        if (estimatedBytes > maxBytes) {
            throw new IllegalArgumentException(message);
        }
    }

    private byte[] fileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new FileStorageException("Không thể đọc file upload", ex);
        }
    }

    private String imageDataUrlExtension(String dataUrl) {
        Matcher matcher = IMAGE_DATA_URL_PATTERN.matcher(dataUrl.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Ảnh không hợp lệ");
        }

        String type = matcher.group(1).toLowerCase(Locale.ROOT);
        return "jpeg".equals(type) ? "jpg" : type;
    }

    private String fileExtension(String filename) {
        String normalized = StringUtils.trimToEmpty(filename);
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == normalized.length() - 1) {
            throw new IllegalArgumentException("File phải có phần mở rộng");
        }

        return normalized.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String joinFolder(String... segments) {
        return String.join(
                "/",
                java.util.Arrays.stream(segments)
                        .map(this::sanitizePathSegment)
                        .filter(StringUtils::isNotBlank)
                        .toList()
        );
    }

    private String sanitizePathSegment(String value) {
        return StringUtils.trimToEmpty(value)
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .replaceAll("[^A-Za-z0-9_./-]", "-");
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
