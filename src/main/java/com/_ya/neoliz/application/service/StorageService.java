package com._ya.neoliz.application.service;

import com._ya.neoliz.global.exception.ProfileBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.region}")
    private String region;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProfileBadRequestException("파일이 비어있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ProfileBadRequestException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ProfileBadRequestException("지원하지 않는 파일 형식입니다. (jpg, png, webp만 허용)");
        }
        String key = "profiles/" + UUID.randomUUID() + "." + extension;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new ProfileBadRequestException("파일 업로드에 실패했습니다.");
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        try {
            String key = fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.error("파일 삭제 중 에러 발생 - URL: {}", fileUrl, e);
        }
    }
}