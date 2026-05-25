package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 시도 기록 엔티티
 * - 사용자가 데일리 퀴즈를 어떻게 풀었는지(시도 횟수, 정답 여부, 포기 여부 등) 기록
 * - (user_id, quiz_id) 조합으로 한 퀴즈당 사용자별 1개 레코드 생성/갱신
 * - 데일리 퀴즈 조회 API에서 사용자의 진행 상태(case 1/2/3)를 분기할 때 사용
 * - createdAt / updatedAt 은 BaseTimeEntity 가 KST 기준으로 자동 관리 (생성 시 + 매 변경 시)
 *
 * 상태 변경은 setter 대신 비즈니스 메서드로 처리 (Service에서 호출).
 */
@Entity
@Table(name = "quiz_attempts")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class QuizAttempt extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)              // 시도한 사용자 ID (FK)
    private Long userId;

    @Column(name = "quiz_id", nullable = false)              // 시도한 퀴즈 ID (FK)
    private Long quizId;

    @Column(name = "attempt_count", nullable = false)        // 현재까지 시도한 횟수 (1~5)
    private Integer attemptCount;

    @Column(name = "is_solved", nullable = false)            // 정답을 맞췄는지 여부
    private Boolean isSolved;

    @Column(name = "hint_used", nullable = false)            // 힌트를 사용했는지 여부 (정답 시 점수 차감용)
    private Boolean hintUsed;

    @Column(name = "is_given_up", nullable = false)          // 포기 버튼을 눌렀는지 여부
    private Boolean isGivenUp;

    @Column(name = "is_finished", nullable = false)          // 퀴즈가 종료되었는지 (정답/5회 오답/포기 중 하나로 종료된 상태)
    private Boolean isFinished;

    @Column(nullable = false)                                // 최종 획득 점수
    private Integer score;

    // ───────────────────────────────────────────────
    //  비즈니스 메서드 (상태 변경은 여기서만)
    // ───────────────────────────────────────────────

    /**
     * 첫 시도 기록을 위한 정적 팩토리.
     * - attemptCount는 호출하는 쪽(Service)에서 0으로 시작해 incrementAttemptCount()로 올린다.
     */
    public static QuizAttempt createNew(Long userId, Long quizId) {
        return QuizAttempt.builder()
                .userId(userId)
                .quizId(quizId)
                .attemptCount(0)
                .isSolved(false)
                .hintUsed(false)
                .isGivenUp(false)
                .isFinished(false)
                .score(0)
                .build();
    }

    /** 시도 횟수 1 증가 (정답 제출/오답 모두에서 호출) */
    public void incrementAttemptCount() {
        this.attemptCount += 1;
    }

    /** 정답 맞춤 처리 — 종료 상태로 전환 + 점수 반영 */
    public void markSolved(int score) {
        this.isSolved = true;
        this.isFinished = true;
        this.score = score;
    }

    /** 5회 오답 자동 종료 처리 — 종료만 표시, 점수 0 */
    public void markFailed() {
        this.isFinished = true;
        this.score = 0;
    }

    /** 포기 처리 — 포기 표시 + 종료 + 점수 0 */
    public void markGivenUp() {
        this.isGivenUp = true;
        this.isFinished = true;
        this.score = 0;
    }

    /** 힌트 사용 표시 (정답 점수 계산 시 차감용) */
    public void useHint() {
        this.hintUsed = true;
    }
}
