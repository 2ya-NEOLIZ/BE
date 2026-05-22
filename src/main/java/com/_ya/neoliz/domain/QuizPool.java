package com._ya.neoliz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 퀴즈 풀(Quiz Pool) 엔티티
 * - 운영자가 미리 등록해둔 모든 데일리 퀴즈 문제의 저장소
 * - 매일 daily_quiz_schedule이 이 풀에서 하나를 골라서 오늘의 퀴즈로 출제
 */
@Entity                                                      // JPA 엔티티임을 표시 (DB 테이블과 매핑)
@Table(name = "quiz_pool")                                   // 매핑되는 DB 테이블명 명시 (스네이크케이스)
@Getter                                                      // 모든 필드에 대한 getter 자동 생성 (Lombok)
@NoArgsConstructor                                           // 파라미터 없는 기본 생성자 자동 생성 (JPA 필수)
@AllArgsConstructor(access = AccessLevel.PRIVATE)            // 모든 필드 받는 생성자, @Builder 내부용으로 private 처리
@Builder                                                     // QuizPool.builder().answer(...).build() 빌더 패턴 자동 생성
public class QuizPool {

    @Id                                                      // PK(기본키) 표시
    @GeneratedValue(strategy = GenerationType.IDENTITY)      // MySQL AUTO_INCREMENT 방식으로 ID 자동 생성
    private Long id;

    @Column(nullable = false, length = 100)                  // NOT NULL, VARCHAR(100) - 퀴즈 정답
    private String answer;

    @Column(nullable = false, length = 20)                   // NOT NULL, VARCHAR(20) - 카테고리 (animal/hufs/game/meme)
    private String category;

    @Column(name = "created_at", nullable = false)           // DB 컬럼명은 created_at, 자바 필드는 createdAt (카멜케이스)
    private LocalDateTime createdAt;

    /**
     * 엔티티가 처음 DB에 INSERT 되기 직전 자동 호출
     * - createdAt 필드를 현재 시각으로 자동 설정해주는 JPA 콜백
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
