package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.CatchGameSession;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이모지 캐치 게임 세션 Repository
 * - PK가 UUID 문자열이므로 ID 타입은 String
 * - 결과 제출 API에서 gameId(=id)로 세션을 조회해 채점에 사용 예정
 */
public interface CatchGameSessionRepository extends JpaRepository<CatchGameSession, String> {
}
