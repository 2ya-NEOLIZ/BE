package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.CatchGameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 이모지 캐치 게임 결과 Repository
 *
 * - countByUserIdAndPlayedAtBetween : 일일 플레이 횟수 집계 (status API)
 * - findByGameId : 게임 시작 시 생성된 결과 row를 gameId로 조회 (결과 제출 시 점수 채움)
 * - findMaxTotalScoreByUserId : 개인 최고 점수 조회 (신기록 판단)
 */
public interface CatchGameResultRepository extends JpaRepository<CatchGameResult, Long> {

    int countByUserIdAndPlayedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    Optional<CatchGameResult> findByGameId(String gameId);

    /** 정상 종료된 결과 중 개인 최고 점수 (없으면 null) */
    @Query("SELECT MAX(r.totalScore) FROM CatchGameResult r " +
            "WHERE r.userId = :userId AND r.isAbandoned = false")
    Integer findMaxTotalScoreByUserId(@Param("userId") Long userId);

    /**
     * 이번 주 캐치 게임 사용자별 최고 점수 랭킹 (전용 랭킹).
     * - 비정상 종료(abandoned) 제외, 주간 범위(played_at) 내
     * - 사용자별 MAX(total_score) 기준 내림차순
     * - 반환: [user_id(Number), best_score(Number)] 행 목록
     */
    @Query(value = """
            SELECT r.user_id, MAX(r.total_score) AS best_score
            FROM catch_game_results r
            WHERE r.is_abandoned = false
              AND r.total_score IS NOT NULL
              AND r.played_at >= :weekStart AND r.played_at <= :weekEnd
            GROUP BY r.user_id
            ORDER BY best_score DESC
            """, nativeQuery = true)
    List<Object[]> findWeeklyBestScores(
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd);
}
