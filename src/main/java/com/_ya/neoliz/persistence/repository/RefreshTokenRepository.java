package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.RefreshToken;
import com._ya.neoliz.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);
    void deleteByUserId(Long userId);

}
