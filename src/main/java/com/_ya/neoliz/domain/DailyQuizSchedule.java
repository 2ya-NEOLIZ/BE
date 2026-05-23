package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 데일리 퀴즈 스케줄 엔티티
 * - 어떤 날짜에 어떤 퀴즈가 출제되는지 기록하는 매핑 테이블
 * - 매일 자정 스케줄러가 quiz_pool에서 미사용 퀴즈를 하나 골라 여기 등록
 * - URL에 quiz_id를 노출하지 않고 "오늘 날짜"로 퀴즈를 찾아오기 위한 구조
 * - createdAt / updatedAt 은 BaseTimeEntity 가 KST 기준으로 자동 관리
 */
@Entity
@Table(name = "daily_quiz_schedule")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DailyQuizSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_date", nullable = false)            // 출제일 (DATE 타입, 시간 정보 X)
    private LocalDate quizDate;

    @Column(name = "quiz_id", nullable = false)              // quiz_pool 테이블의 id를 가리키는 FK 역할 (지금은 Long으로만 두고 추후 @ManyToOne으로 리팩토링 가능)
    private Long quizId;
}
