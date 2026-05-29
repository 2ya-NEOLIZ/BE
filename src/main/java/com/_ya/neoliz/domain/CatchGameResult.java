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
 * - 종료된 캐치 게임 세션의 결과를 기록
 * - 일일 플레이 횟수 조회(/catch/status) / 주간 랭킹 집계의 기준이 됨
 * - createdAt / updatedAt 은 BaseTimeEntity 가 KST 기준으로 자동 관리
 *
 * 이번 이슈(#47)에서는 status 조회에 필요한 최소 필드(userId, playedAt)만 둔다.
 * 게임 결과 제출 API 구현 이슈에서 totalScore / maxCombo / perfectCount /
 * goodCount / missCount / isAbandoned / gameId 등이 추가 예정.
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

    @Column(name = "played_at", nullable = false)       // 게임 종료 시각 (KST 기준 LocalDateTime)
    private LocalDateTime playedAt;
}
