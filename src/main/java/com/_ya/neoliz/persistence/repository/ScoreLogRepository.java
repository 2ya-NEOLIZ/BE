package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.ScoreLog;
import com._ya.neoliz.domain.ScoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScoreLogRepository extends JpaRepository<ScoreLog, Long> {

    @Query(value = """
          SELECT sl.user_id, SUM(sl.score) as total_score
          FROM score_logs sl
          WHERE sl.created_at >= :weekStart AND sl.created_at <= :weekEnd
          GROUP BY sl.user_id
          ORDER BY total_score DESC
          """, nativeQuery = true)
    List<Object[]> findAllWeeklyScores(
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd);

    @Query("SELECT COUNT(sl) FROM ScoreLog sl WHERE sl.userId = :userId AND sl.scoreType = :scoreType AND sl.createdAt >= :dayStart AND sl.createdAt <= :dayEnd")
    long countTodayScoreByType(
            @Param("userId") Long userId,
            @Param("scoreType") ScoreType scoreType,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);

}
