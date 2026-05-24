package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 시퀀스 아이템 엔티티
 * - 한 퀴즈가 어떤 이모지들로 구성되는지 순서대로 저장
 * - 예: quiz_id=42 → [이모지A(0), 이모지B(1), 이모지C(2)] 식으로 시퀀스 구성
 * - order_index 기준으로 정렬해서 사용
 */
@Entity
@Table(name = "quiz_sequence_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class QuizSequenceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)              // 어떤 퀴즈에 속하는 아이템인지 (FK)
    private Long quizId;

    @Column(name = "emoji_id", nullable = false)             // 어떤 이모지인지 (FK, Emoji 엔티티는 주희가 만들 예정)
    private Long emojiId;

    @Column(name = "order_index", nullable = false)          // 시퀀스 내 순서 (0부터 시작하는 인덱스)
    private Integer orderIndex;
}
