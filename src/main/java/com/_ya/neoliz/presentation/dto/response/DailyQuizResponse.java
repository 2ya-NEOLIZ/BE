package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.QuizAttempt;
import com._ya.neoliz.domain.QuizPool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * 데일리 퀴즈 조회 API 응답 DTO
 *
 * API 명세상 사용자 상태에 따라 응답 case가 3가지로 분기됨:
 *   - case 1: 아직 시도하지 않음          → isSolved=false, isFinished=false, answer/score = null
 *   - case 2: 이미 정답을 맞춤             → isSolved=true,  isFinished=true,  answer/score 노출
 *   - case 3: 5회 시도 모두 사용 (포기 포함) → isSolved=false, isFinished=true,  answer/score 노출
 *
 * 응답 메시지/필드 분기 로직은 정적 팩토리 메서드 {@link #of(QuizPool, List, Optional)} 안에서 처리.
 */
@Getter
@AllArgsConstructor                 // 모든 필드 받는 생성자 (@Builder가 내부적으로 사용)
@Builder                            // DailyQuizResponse.builder().quizId(...).build() 패턴
public class DailyQuizResponse {

    /** 한 퀴즈당 최대 시도 횟수 (상수) */
    public static final int MAX_ATTEMPTS = 5;

    /** 퀴즈 PK (quiz_pool.id) */
    private Long quizId;

    /** 이모지 시퀀스 (order_index 오름차순) — 안에 emojiId/imageUrl/soundUrl */
    private List<EmojiInfo> sequence;

    /** 최대 시도 횟수 (현재는 5 고정) */
    private int maxAttempts;

    /** 남은 시도 횟수 (= maxAttempts - attempt.attemptCount, 최소 0) */
    private int remainingAttempts;

    /** 정답을 맞췄는지 여부 */
    private Boolean isSolved;

    /** 퀴즈가 종료됐는지 여부 (정답 / 5회 오답 / 포기 중 하나) */
    private Boolean isFinished;

    /** 정답 텍스트 — 종료된 상태(case 2, 3)에서만 채워짐. 평소엔 null */
    private String answer;

    /** 획득 점수 — 종료된 상태(case 2, 3)에서만 채워짐. 평소엔 null */
    private Integer score;

    /**
     * 도메인 객체들로부터 응답 DTO 조립
     *
     * @param quiz         오늘의 퀴즈 (QuizPool)
     * @param sequence     변환 완료된 이모지 시퀀스 (Service에서 emoji 정보 채워서 넘겨줌)
     * @param attemptOpt   사용자의 시도 기록 (Optional — 미시도면 empty)
     */
    public static DailyQuizResponse of(
            QuizPool quiz,
            List<EmojiInfo> sequence,
            Optional<QuizAttempt> attemptOpt
    ) {
        // 공통 필드 빌더 (quizId, sequence, maxAttempts)
        DailyQuizResponseBuilder builder = DailyQuizResponse.builder()
                .quizId(quiz.getId())
                .sequence(sequence)
                .maxAttempts(MAX_ATTEMPTS);

        // ─── case 1: 아직 시도하지 않은 사용자 ───
        if (attemptOpt.isEmpty()) {
            return builder
                    .remainingAttempts(MAX_ATTEMPTS)
                    .isSolved(false)
                    .isFinished(false)
                    // answer, score는 null로 둠 (필드 기본값)
                    .build();
        }

        QuizAttempt attempt = attemptOpt.get();
        // 남은 횟수 계산 (음수 방지)
        int remaining = Math.max(0, MAX_ATTEMPTS - attempt.getAttemptCount());

        // ─── case 2 / case 3: 종료된 상태 ───
        if (Boolean.TRUE.equals(attempt.getIsFinished())) {
            return builder
                    .remainingAttempts(remaining)
                    .isSolved(attempt.getIsSolved())   // case 2면 true, case 3이면 false
                    .isFinished(true)
                    .answer(quiz.getAnswer())          // 정답 공개
                    .score(attempt.getScore())         // 점수 공개
                    .build();
        }

        // ─── case 1.5: 진행 중 (시도는 했지만 종료 안 됨) ───
        return builder
                .remainingAttempts(remaining)
                .isSolved(false)
                .isFinished(false)
                .build();
    }

    /**
     * 이모지 시퀀스의 한 요소
     * - 응답 JSON의 sequence 배열 안에 들어가는 객체
     */
    @Getter
    @AllArgsConstructor
    @Builder
    public static class EmojiInfo {
        private Long emojiId;
        private String imageUrl;
        private String soundUrl;
    }
}
