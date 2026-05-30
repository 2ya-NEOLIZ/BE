package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이모지 캐치 게임 세션 엔티티
 * - 게임 시작 시 생성되며, 진행 중인 게임의 라운드 데이터를 보관
 * - id 는 UUID v4 문자열 (PK), 게임 시작 시 발급한 gameId
 * - rounds 는 10라운드 데이터를 JSON 컬럼으로 저장 (결과 제출 시 서버 채점 검증에 사용)
 * - createdAt / updatedAt 은 BaseTimeEntity 가 KST 기준으로 자동 관리
 */
@Entity
@Table(name = "catch_game_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CatchGameSession extends BaseTimeEntity {

    /** 게임 세션 식별자 (UUID v4, 애플리케이션에서 생성) */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)             // 게임을 시작한 사용자 ID (FK)
    private Long userId;

    /** 10라운드 데이터 (JSON 컬럼) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rounds_data", columnDefinition = "json", nullable = false)
    private List<CatchRound> rounds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)  // PLAYING / FINISHED / ABANDONED / EXPIRED
    private CatchGameStatus status;

    @Column(name = "expires_at", nullable = false)           // 세션 만료 시각 (KST)
    private LocalDateTime expiresAt;

    /**
     * 새 게임 세션 생성용 정적 팩토리.
     * - 시작 직후 상태는 PLAYING
     */
    public static CatchGameSession createNew(String gameId, Long userId,
                                             List<CatchRound> rounds, LocalDateTime expiresAt) {
        return CatchGameSession.builder()
                .id(gameId)
                .userId(userId)
                .rounds(rounds)
                .status(CatchGameStatus.PLAYING)
                .expiresAt(expiresAt)
                .build();
    }
}
