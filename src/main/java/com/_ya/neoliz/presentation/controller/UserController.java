package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.ProfileService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.ProfileImageResponse;
import com._ya.neoliz.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "사용자", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/v1/neoliz/users")
@RequiredArgsConstructor
public class UserController {
    private final ProfileService profileService;
    @Operation(
            summary = "내 정보 조회",
            description = "내 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Long userId) {
        UserResponse userResponse = profileService.findById(userId);
        return ResponseEntity.ok(ApiResponse.success("조회 완료", userResponse));
    }
    @Operation(
            summary = "프로필 이미지 수정",
            description = "프로필 이미지를 수정합니다."
    )
    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // consumes: 멀티 폼 데이터 형식으로 받기 위해 꼭 설정
    public ResponseEntity<ApiResponse<ProfileImageResponse>> updateProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestPart(value = "image") MultipartFile image
    ) {
        String newImageUrl = profileService.updateProfileImage(userId, image);
        ProfileImageResponse profileImageResponse = new ProfileImageResponse(newImageUrl);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지 변경 성공", profileImageResponse));
    }
}
