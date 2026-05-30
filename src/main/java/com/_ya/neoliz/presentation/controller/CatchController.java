package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.CatchService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.request.SubmitCatchResultRequest;
import com._ya.neoliz.presentation.dto.response.CatchStatusResponse;
import com._ya.neoliz.presentation.dto.response.StartCatchGameResponse;
import com._ya.neoliz.presentation.dto.response.SubmitCatchResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * POST /api/v1/neoliz/catch/start
     *
     * 이모지 캐치 게임을 시작한다.
     * 잔여 플레이 횟수를 차감하고 게임 세션(gameId)과 10라운드 데이터를 발급.
     */
    @Operation(
            summary = "게임 시작",
            description = "이모지 캐치 게임을 시작합니다. 잔여 플레이 횟수를 차감하고 " +
                    "게임 세션(gameId)과 10라운드 데이터를 한 번에 반환합니다.<br>" +
                    "  - 게임 시작 시점에 횟수 차감 (중간 이탈해도 사용 처리)<br>" +
                    "  - 라운드가 진행될수록 판정 영역이 좁아지고 바 속도가 빨라짐<br>" +
                    "예외 응답:<br>" +
                    "  - 오늘 플레이 횟수 초과: 403 Forbidden (\"오늘 플레이 횟수를 모두 사용했습니다.\")"
    )
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StartCatchGameResponse>> startGame(
            @AuthenticationPrincipal Long userId) {
        StartCatchGameResponse response = catchService.startGame(userId);
        return ResponseEntity.ok(ApiResponse.success("게임 시작 성공", response));
    }

    /**
     * POST /api/v1/neoliz/catch/submit
     *
     * 게임 종료 후 결과를 제출한다.
     * 서버 점수 검증 후 결과 저장 + 개인 신기록 판단 + 주간 랭킹 TOP5 반환.
     * abandoned=true 면 0점 처리되고 랭킹에 반영되지 않는다.
     */
    @Operation(
            summary = "게임 결과 제출",
            description = "게임 종료 후 결과를 제출합니다. 서버에서 점수를 검증하고 결과 저장 + " +
                    "개인 신기록 판단 + 주간 랭킹(TOP5)을 반환합니다.<br>" +
                    "  - abandoned=true: 비정상 종료 → 0점 처리, 랭킹 미반영<br>" +
                    "  - 주간 랭킹은 공통 랭킹(주간 점수 합산) 기준<br>" +
                    "예외 응답:<br>" +
                    "  - gameId 없음/만료: 400 Bad Request<br>" +
                    "  - 본인 게임 세션이 아님: 403 Forbidden<br>" +
                    "  - 이미 제출된 게임: 409 Conflict<br>" +
                    "  - 점수 검증 실패: 400 Bad Request"
    )
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SubmitCatchResultResponse>> submitResult(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SubmitCatchResultRequest request) {
        SubmitCatchResultResponse response = catchService.submitResult(userId, request);
        String message = Boolean.TRUE.equals(request.getAbandoned())
                ? "게임 중단 처리 완료"
                : "게임 결과 제출 성공";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
