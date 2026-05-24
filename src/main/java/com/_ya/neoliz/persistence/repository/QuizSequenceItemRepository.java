package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.QuizSequenceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 퀴즈 시퀀스 아이템(quiz_sequence_items) 테이블 접근 리포지토리
 */
public interface QuizSequenceItemRepository extends JpaRepository<QuizSequenceItem, Long> {

    /**
     * 특정 퀴즈에 속하는 모든 이모지 시퀀스를 순서대로(order_index 오름차순) 조회
     *
     * 메서드 이름 규칙으로 자동 생성되는 SQL:
     *   findByQuizIdOrderByOrderIndexAsc(quizId)
     *   → SELECT * FROM quiz_sequence_items
     *     WHERE quiz_id = ?
     *     ORDER BY order_index ASC
     *
     * @param quizId 조회할 퀴즈 ID
     * @return 순서대로 정렬된 시퀀스 아이템 목록
     */
    List<QuizSequenceItem> findByQuizIdOrderByOrderIndexAsc(Long quizId);
}
