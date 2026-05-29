package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.QuizService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.request.SubmitQuizRequest;
import com._ya.neoliz.presentation.dto.response.DailyQuizResponse;
import com._ya.neoliz.presentation.dto.response.SubmitQuizResponse;
import com._ya.neoliz.presentation.dto.response.UseHintResponse;
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
 * 이모지 퀴즈 Controller
 *
 * - 베이스 URL: /api/v1/neoliz/quiz
 * - 데일리 퀴즈 조회/제출/힌트 엔드포인트 담당
 * - 모든 메서드는 JWT 인증 필요 (SecurityConfig 기본 정책)
 */
@Tag(name = "이모지 퀴즈", description = "데일리 이모지 퀴즈 관련 API")
@RestController
@RequestMapping("/api/v1/neoliz/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * GET /api/v1/neoliz/quiz/daily
     *
     * 오늘의 데일리 퀴즈 + 사용자 진행 상태 반환.
     * 사용자 상태에 따라 응답 case 3가지 (DailyQuizResponse 참고).
     */
    @Operation(
            summary = "데일리 퀴즈 조회",
            description = "오늘의 데일리 이모지 퀴즈와 사용자 진행 상태를 함께 조회합니다. " +
                    "응답은 사용자 상태에 따라 3가지 case로 분기됩니다:<br>" +
                    "  - case 1: 아직 시도하지 않은 상태 (isSolved=false, isFinished=false)<br>" +
                    "  - case 2: 이미 정답을 맞춘 상태 (isSolved=true, answer 노출)<br>" +
                    "  - case 3: 5회 시도 모두 사용 (isFinished=true, answer 노출, score=0)"
    )
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyQuizResponse>> getDailyQuiz(
            @AuthenticationPrincipal Long userId) {
        DailyQuizResponse response = quizService.getDailyQuiz(userId);
        return ResponseEntity.ok(ApiResponse.success("오늘의 퀴즈 조회 성공", response));
    }

    /**
     * POST /api/v1/neoliz/quiz/daily/submit
     *
     * 데일리 퀴즈 정답 제출 또는 포기.
     * Request의 isGivenUp 값과 정답 일치 여부에 따라 응답 case 4가지로 분기.
     */
    @Operation(
            summary = "데일리 퀴즈 정답 제출",
            description = "오늘의 데일리 퀴즈에 대한 정답을 제출하거나 포기 처리합니다. " +
                    "응답은 사용자 입력에 따라 4가지 case로 분기됩니다:<br>" +
                    "  - case 1: 정답 (isCorrect=true, attemptCount, score)<br>" +
                    "  - case 2: 오답 + 시도 가능 (isCorrect=false, remainingAttempts)<br>" +
                    "  - case 3: 5회째 오답 → 자동 종료 (isFinished=true, answer, score=0)<br>" +
                    "  - case 4: 포기 (isGivenUp=true, isFinished=true, answer, score=0)<br><br>" +
                    "이미 종료된 퀴즈(정답/포기/5회 종료)에 다시 호출 시 409 Conflict 응답."
    )
    @PostMapping("/daily/submit")
    public ResponseEntity<ApiResponse<SubmitQuizResponse>> submitQuiz(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SubmitQuizRequest request) {

        SubmitQuizResponse response = quizService.submitQuiz(userId, request);

        // case별 메시지 분기 (API 명세서 기준)
        String message = resolveMessage(response);
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * POST /api/v1/neoliz/quiz/daily/hint
     *
     * 오늘의 데일리 퀴즈 힌트(카테고리) 조회.
     * 1회 이상 오답 시도한 후 사용 가능하며, 퀴즈당 1회만 사용할 수 있다.
     */
    @Operation(
            summary = "데일리 퀴즈 힌트 조회",
            description = "오늘의 데일리 퀴즈 카테고리 힌트를 조회합니다. " +
                    "힌트는 1회 이상 오답 시도한 후 사용 가능하며, 퀴즈당 1회만 사용 가능합니다.<br>" +
                    "예외 응답:<br>" +
                    "  - 1회도 시도하지 않은 경우: 403 Forbidden (\"1회 오답 후 사용 가능합니다.\")<br>" +
                    "  - 이미 종료된 퀴즈 (정답/포기/5회): 409 Conflict<br>" +
                    "  - 이미 힌트를 사용한 경우: 409 Conflict"
    )
    @PostMapping("/daily/hint")
    public ResponseEntity<ApiResponse<UseHintResponse>> useHint(
            @AuthenticationPrincipal Long userId) {
        UseHintResponse response = quizService.useHint(userId);
        return ResponseEntity.ok(ApiResponse.success("힌트 조회 성공", response));
    }

    /**
     * 응답 case에 따라 ApiResponse.message 결정.
     * API 명세서의 각 case별 메시지 그대로 매핑.
     */
    private String resolveMessage(SubmitQuizResponse response) {
        if (Boolean.TRUE.equals(response.getIsGivenUp())) {
            return "포기 처리 완료";
        }
        if (Boolean.TRUE.equals(response.getIsFinished())
                && !Boolean.TRUE.equals(response.getIsCorrect())) {
            return "기회를 모두 사용했습니다";
        }
        if (Boolean.TRUE.equals(response.getIsCorrect())) {
            return "정답 제출 성공";
        }
        return "정답 제출 성공";
    }
}
