package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.QuizPool;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 퀴즈 풀(quiz_pool) 테이블 접근 리포지토리
 *
 * JpaRepository<엔티티 타입, ID 타입>을 상속받으면
 * 다음과 같은 기본 CRUD 메서드가 자동으로 제공됨:
 *   - save(entity)        : INSERT or UPDATE
 *   - findById(id)        : PK로 단건 조회 (Optional 반환)
 *   - findAll()           : 전체 조회
 *   - deleteById(id)      : PK로 삭제
 *   - count()             : 전체 개수
 *
 * QuizPool은 기본 CRUD 외 별도 조회 조건이 필요 없어서 비어 있음.
 * (퀴즈 ID로 조회할 땐 기본 제공되는 findById 사용)
 */
public interface QuizPoolRepository extends JpaRepository<QuizPool, Long> {
}
