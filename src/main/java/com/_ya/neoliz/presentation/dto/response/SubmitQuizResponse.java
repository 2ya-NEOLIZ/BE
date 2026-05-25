package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 데일리 퀴즈 정답 제출 API 응답 DTO
 *
 * 사용자 입력에 따라 응답 case 4가지로 분기됨:
 *   - case 1: 정답              → isCorrect=true,  attemptCount, score
 *   - case 2: 오답 (시도 가능)  → isCorrect=false, remainingAttempts
 *   - case 3: 5회째 오답 (종료) → isCorrect=false, isFinished=true, answer, score=0
 *   - case 4: 포기              → isGivenUp=true,  isFinished=true, answer, score=0
 *
 * 한 DTO에 모든 필드를 담고, 해당하지 않는 필드는 null로 처리한다.
 * (Jackson 기본 설정상 null 필드도 응답에 포함됨)
 *
 * 응답 메시지(ApiResponse.message)는 Controller에서 case에 따라 다르게 넣어준다.
 */
@Getter
@AllArgsConstructor
@Builder
public class SubmitQuizResponse {

    /** 정답 여부 (case 1: true, 2/3: false, 4(포기)에서는 null) */
    private Boolean isCorrect;

    /** 정답을 맞추기까지 시도한 횟수 (case 1에서만 채움, 나머지 null) */
    private Integer attemptCount;

    /** 남은 시도 횟수 (case 2/3에서만 채움) */
    private Integer remainingAttempts;

    /** 퀴즈 종료 여부 (case 3/4: true) */
    private Boolean isFinished;

    /** 포기 여부 (case 4: true) */
    private Boolean isGivenUp;

    /** 정답 공개 (case 3/4 → 종료된 상태에서만 노출) */
    private String answer;

    /** 획득 점수 (case 1: 계산된 점수, case 3/4: 0, case 2: null) */
    private Integer score;

    // ───────────────────────────────────────────────
    //  case 별 정적 팩토리 메서드
    // ───────────────────────────────────────────────

    /** case 1: 정답 맞춤 */
    public static SubmitQuizResponse correct(int attemptCount, int score) {
        return SubmitQuizResponse.builder()
                .isCorrect(true)
                .attemptCount(attemptCount)
                .score(score)
                .build();
    }

    /** case 2: 오답 + 시도 가능 (아직 5회 안 됨) */
    public static SubmitQuizResponse wrong(int remainingAttempts) {
        return SubmitQuizResponse.builder()
                .isCorrect(false)
                .remainingAttempts(remainingAttempts)
                .build();
    }

    /** case 3: 5회째 오답 (자동 종료) */
    public static SubmitQuizResponse wrongAndFinished(String answer) {
        return SubmitQuizResponse.builder()
                .isCorrect(false)
                .remainingAttempts(0)
                .isFinished(true)
                .answer(answer)
                .score(0)
                .build();
    }

    /** case 4: 포기 */
    public static SubmitQuizResponse givenUp(String answer) {
        return SubmitQuizResponse.builder()
                .isGivenUp(true)
                .isFinished(true)
                .answer(answer)
                .score(0)
                .build();
    }
}
