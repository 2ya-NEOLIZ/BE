package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.User;
import com._ya.neoliz.global.exception.UserNotFoundException;
import com._ya.neoliz.persistence.repository.UserRepository;
import com._ya.neoliz.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final StorageService storageService;
    private static final String defaultImageUrl = "디폴트 이미지 링크";

    @Transactional(readOnly = true)
    public UserResponse findById(Long id){
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("조회 실패"));
        return UserResponse.from(user);
    }

    @Transactional
    public String updateProfileImage(Long id, MultipartFile image) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("조회 실패")); // 404
        String oldImageUrl = user.getProfileImageUrl();
        String newImageUrl = storageService.upload(image);
        user.updateProfileImage(newImageUrl);
        if (oldImageUrl != null && !oldImageUrl.isEmpty() && !oldImageUrl.equals(defaultImageUrl)) {
            try {
                log.info("DB 플러시 완료: 스토리지에서 기존 파일 삭제 시도 -> URL: {}", oldImageUrl);
                storageService.delete(oldImageUrl);
            } catch (Exception e) {
                log.error("기존 프로필 이미지 스토리지 삭제 실패 - URL: {}", oldImageUrl, e); // 신규 등록에 영향 X
            }
        }

        return newImageUrl;
    }
}
