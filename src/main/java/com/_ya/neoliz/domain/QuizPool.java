package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 풀(Quiz Pool) 엔티티
 * - 운영자가 미리 등록해둔 모든 데일리 퀴즈 문제의 저장소
 * - 매일 daily_quiz_schedule이 이 풀에서 하나를 골라서 오늘의 퀴즈로 출제
 * - createdAt / updatedAt 은 BaseTimeEntity 가 KST(Asia/Seoul) 기준으로 자동 관리
 */
@Entity                                                      // JPA 엔티티임을 표시 (DB 테이블과 매핑)
@Table(name = "quiz_pool")                                   // 매핑되는 DB 테이블명 명시
@Getter                                                      // 모든 필드에 대한 getter 자동 생성 (Lombok)
@NoArgsConstructor                                           // 파라미터 없는 기본 생성자 자동 생성 (JPA 필수)
@AllArgsConstructor(access = AccessLevel.PRIVATE)            // @Builder 내부용 전체 필드 생성자 (외부 노출 X)
@Builder                                                     // QuizPool.builder().answer(...).build() 빌더 패턴 자동 생성
public class QuizPool extends BaseTimeEntity {               // 시각 필드는 부모(BaseTimeEntity)에서 상속

    @Id                                                      // PK(기본키) 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)      // MySQL AUTO_INCREMENT 방식으로 ID 자동 생성
    private Long id;

    @Column(nullable = false, length = 100)                  // NOT NULL, VARCHAR(100) - 퀴즈 정답
    private String answer;

    @Column(nullable = false, length = 20)                   // NOT NULL, VARCHAR(20) - 카테고리 (animal/hufs/game/meme)
    private String category;

    @Column(length = 200)
    private String hint;
}
