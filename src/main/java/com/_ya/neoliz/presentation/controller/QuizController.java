package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.QuizService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.DailyQuizResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 이모지 퀴즈 Controller
 *
 * - 베이스 URL: /api/v1/neoliz/quiz
 * - 데일리 퀴즈 조회/제출/힌트 엔드포인트 담당 (현재는 조회만 구현)
 */
@Tag(name = "이모지 퀴즈", description = "데일리 이모지 퀴즈 관련 API")  // Swagger UI 그룹 이름
@RestController                                  // @Controller + @ResponseBody (응답을 JSON으로)
@RequestMapping("/api/v1/neoliz/quiz")           // 베이스 URL (메서드 매핑 앞에 자동으로 붙음)
@RequiredArgsConstructor                         // final 필드 생성자 자동 생성 (의존성 주입)
public class QuizController {

    private final QuizService quizService;

    /**
     * GET /api/v1/neoliz/quiz/daily
     *
     * 오늘의 데일리 퀴즈 + 사용자 진행 상태 반환.
     * 사용자 상태에 따라 응답 case 3가지 (DailyQuizResponse 참고).
     *
     * TODO: JWT 도입 시 userId를 @RequestParam이 아니라 토큰에서 추출하도록 변경
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
}
