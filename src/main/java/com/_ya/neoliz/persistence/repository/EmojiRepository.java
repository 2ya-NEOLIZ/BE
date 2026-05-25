package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.Category;
import com._ya.neoliz.domain.Emoji;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {
    List<Emoji> findTop12ByCategory(Category category);
}
