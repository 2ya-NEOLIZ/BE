package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.Category;
import com._ya.neoliz.domain.Emoji;
import com._ya.neoliz.global.exception.InvalidCategoryException;
import com._ya.neoliz.persistence.repository.EmojiRepository;
import com._ya.neoliz.presentation.dto.response.EmojiListResponse;
import com._ya.neoliz.presentation.dto.response.EmojiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmojiService {
    private final EmojiRepository emojiRepository;

    public EmojiListResponse getEmojisByCategory(String category) {
        Category categoryEnum;
        try {
            categoryEnum = Category.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException("유효하지 않은 카테고리입니다: " + category);
        }

        List<Emoji> emojis = emojiRepository.findTop12ByCategory(categoryEnum);
        List<EmojiResponse> emojiResponses = emojis.stream()
                .map(EmojiResponse::from)
                .toList();

        return EmojiListResponse.of(category, emojiResponses);
    }
}
