package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.Category;
import com._ya.neoliz.domain.Emoji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {
    List<Emoji> findTop12ByCategory(Category category);

    /**
     * 이모지 풀에서 중복 없이 랜덤으로 count개 조회 (이모지 캐치 게임 라운드 생성용)
     * - MySQL ORDER BY RAND() 사용
     */
    @Query(value = "SELECT * FROM emojis ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Emoji> findRandomEmojis(@Param("count") int count);
}
