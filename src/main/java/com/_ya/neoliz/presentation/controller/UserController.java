package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.ProfileService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

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
    public ResponseEntity<?> getProfile() {
        Long id = 1L;   // findById()를 위한 임시 데이터
        UserResponse userResponse = profileService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("조회 완료", userResponse));
    }
}
