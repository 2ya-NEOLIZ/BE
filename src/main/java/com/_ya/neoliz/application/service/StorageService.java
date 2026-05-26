package com._ya.neoliz.application.service;

import com._ya.neoliz.global.exception.ProfileBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class StorageService {
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
        String uniqueFilename = UUID.randomUUID() + "." + extension;
        return "temp.url/" + uniqueFilename; // 임시 반환값
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        // S3 연동 X -> 현재 시점에서는 로그만 출력(추후 연동 필요)
        try {
            log.info("파일 삭제 로직 호출: {}", fileUrl);
        } catch (Exception e) {
            log.error("파일 삭제 중 에러 발생 - URL: {}", fileUrl, e);
        }
    }
}