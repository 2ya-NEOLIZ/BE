package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);
    boolean existsByNicknameAndIdNot(String nickname, Long id);

    /**
     * 사용자 row에 비관적 락(SELECT ... FOR UPDATE)을 걸어 조회.
     * 동일 유저의 동시 요청을 트랜잭션 단위로 직렬화해야 하는 경우 사용
     * (예: 캐치 게임 일일 플레이 횟수 체크-후-삽입의 원자성 보장).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

}