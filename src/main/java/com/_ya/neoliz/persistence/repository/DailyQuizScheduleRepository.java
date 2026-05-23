package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.DailyQuizSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 데일리 퀴즈 스케줄(daily_quiz_schedule) 테이블 접근 리포지토리
 */
public interface DailyQuizScheduleRepository extends JpaRepository<DailyQuizSchedule, Long> {

    /**
     * 특정 날짜에 출제된 퀴즈 스케줄을 조회
     *
     * 메서드 이름 규칙(Query Method)으로 SQL 자동 생성:
     *   findByQuizDate(date)
     *   → SELECT * FROM daily_quiz_schedule WHERE quiz_date = ?
     *
     * 해당 날짜에 출제된 퀴즈가 없을 수도 있으므로 Optional로 감싸서 반환.
     *
     * @param quizDate 조회할 출제일 (KST 자정 기준)
     * @return 출제된 퀴즈 스케줄 (없으면 Optional.empty())
     */
    Optional<DailyQuizSchedule> findByQuizDate(LocalDate quizDate);
}
