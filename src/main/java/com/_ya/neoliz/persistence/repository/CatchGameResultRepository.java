package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.CatchGameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
}
