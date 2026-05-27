package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);
    boolean existsByNicknameAndIdNot(String nickname, Long id);

}