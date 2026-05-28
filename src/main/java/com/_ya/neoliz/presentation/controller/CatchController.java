package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.CatchService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.CatchStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이모지 캐치 Controller
 *
 * - 베이스 URL: /api/v1/neoliz/catch
 * - 플레이 상태 조회 / 게임 시작 / 결과 제출 엔드포인트 담당 예정
 * - 모든 메서드는 JWT 인증 필요 (SecurityConfig 기본 정책)
 */
@Tag(name = "이모지 캐치", description = "이모지 캐치 게임 관련 API")
@RestController
@RequestMapping("/api/v1/neoliz/catch")
@RequiredArgsConstructor
public class CatchController {

    private final CatchService catchService;

    /**
     * GET /api/v1/neoliz/catch/status
     *
     * 오늘 사용자의 이모지 캐치 플레이 가능 여부와 남은 횟수를 반환.
     * 최대 3회 / KST 자정 기준 일일 카운트.
     */
    @Operation(
            summary = "오늘 플레이 가능 횟수 조회",
            description = "사용자의 오늘 이모지 캐치 플레이 가능 여부와 남은 횟수를 반환합니다.<br>" +
                    "  - 일일 최대 3회 / KST 자정 기준 집계<br>" +
                    "  - isPlayable: 남은 횟수가 1회 이상이면 true<br>" +
                    "  - remainingPlays: 오늘 남은 플레이 횟수<br>" +
                    "  - maxPlays: 일일 최대 플레이 횟수 (3)"
    )
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<CatchStatusResponse>> getStatus(
            @AuthenticationPrincipal Long userId) {
        CatchStatusResponse response = catchService.getStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("플레이 횟수 조회 성공", response));
    }
}
