package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이모지 캐치 게임 결과 엔티티
 * - 게임 시작 시(#50) 잔여 횟수 차감용으로 row 생성, 결과 제출 시(#55) 점수/통계가 채워짐
 * - 일일 플레이 횟수 조회(/catch/status) / 주간 랭킹 집계의 기준이 됨
 * - createdAt / updatedAt 은 BaseTimeEntity 가 KST 기준으로 자동 관리
 *
 * 점수 관련 컬럼은 nullable — 게임 시작 시점엔 채워지지 않고 결과 제출 시 채워진다.
 */
@Entity
@Table(name = "catch_game_results")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CatchGameResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)         // 게임을 플레이한 사용자 ID (FK)
    private Long userId;

    @Column(name = "played_at", nullable = false)       // 게임 시작 시각 (KST 기준 LocalDateTime)
    private LocalDateTime playedAt;

    @Column(name = "game_id", length = 36)              // 연결된 게임 세션 ID (UUID)
    private String gameId;

    @Column(name = "total_score")                       // 총 점수 (결과 제출 시 채움, abandoned 시 0)
    private Integer totalScore;

    @Column(name = "max_combo")                         // 최대 콤보 (0 ~ 10)
    private Integer maxCombo;

    @Column(name = "perfect_count")                     // PERFECT 판정 횟수
    private Integer perfectCount;

    @Column(name = "good_count")                        // GOOD 판정 횟수
    private Integer goodCount;

    @Column(name = "miss_count")                        // MISS 판정 횟수
    private Integer missCount;

    @Column(name = "is_abandoned")                      // 비정상 종료 여부 (true면 점수 0/랭킹 미반영)
    private Boolean isAbandoned;

    /**
     * 게임 시작 시점 기록 (#50) — 횟수 차감용. 점수 컬럼은 비워둔다.
     */
    public static CatchGameResult start(Long userId, String gameId, LocalDateTime playedAt) {
        return CatchGameResult.builder()
                .userId(userId)
                .gameId(gameId)
                .playedAt(playedAt)
                .build();
    }

    /**
     * 결과 제출 시 점수/통계 채우기 (정상 종료)
     */
    public void finish(int totalScore, int maxCombo, int perfectCount, int goodCount, int missCount) {
        this.totalScore = totalScore;
        this.maxCombo = maxCombo;
        this.perfectCount = perfectCount;
        this.goodCount = goodCount;
        this.missCount = missCount;
        this.isAbandoned = false;
    }

    /**
     * 결과 제출 시 비정상 종료 처리 (점수 0, 랭킹 미반영)
     */
    public void abandon() {
        this.totalScore = 0;
        this.maxCombo = 0;
        this.perfectCount = 0;
        this.goodCount = 0;
        this.missCount = 0;
        this.isAbandoned = true;
    }
}
