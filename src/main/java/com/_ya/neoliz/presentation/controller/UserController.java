package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.ProfileService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
}
