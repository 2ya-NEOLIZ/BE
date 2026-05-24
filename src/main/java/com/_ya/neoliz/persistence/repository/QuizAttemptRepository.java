package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 퀴즈 시도 기록(quiz_attempts) 테이블 접근 리포지토리
 */
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    /**
     * 특정 사용자가 특정 퀴즈에 대해 남긴 시도 기록을 조회
     *
     * 메서드 이름 규칙으로 자동 생성되는 SQL:
     *   findByUserIdAndQuizId(userId, quizId)
     *   → SELECT * FROM quiz_attempts
     *     WHERE user_id = ? AND quiz_id = ?
     *
     * 한 번도 시도하지 않은 경우(첫 진입)에는 기록이 없을 수 있으므로 Optional로 감싸서 반환.
     *
     * @param userId 조회할 사용자 ID
     * @param quizId 조회할 퀴즈 ID
     * @return 시도 기록 (없으면 Optional.empty() → 데일리 퀴즈 조회 응답의 case 1)
     */
    Optional<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
}
