package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.CatchGameResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * 이모지 캐치 게임 결과 Repository
 *
 * - countByUserIdAndPlayedAtBetween : 일일 플레이 횟수 집계 (status API)
 *   start (포함) ~ end (제외) 범위로 카운트. KST 자정 ~ 다음날 자정.
 */
public interface CatchGameResultRepository extends JpaRepository<CatchGameResult, Long> {

    int countByUserIdAndPlayedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
