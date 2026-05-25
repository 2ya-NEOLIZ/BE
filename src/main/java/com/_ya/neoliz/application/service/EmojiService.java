package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.Category;
import com._ya.neoliz.domain.Emoji;
import com._ya.neoliz.global.exception.EmojiNotFoundException;
import com._ya.neoliz.global.exception.InvalidCategoryException;
import com._ya.neoliz.persistence.repository.EmojiRepository;
import com._ya.neoliz.presentation.dto.response.EmojiDetailResponse;
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

    // 1. 카테고리별 이모지 12개 조회
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

    // 2. 이모지 단건 조회
    public EmojiDetailResponse getEmojiById(Long emojiId) {
        Emoji emoji = emojiRepository.findById(emojiId)
                .orElseThrow(() -> new EmojiNotFoundException("존재하지 않는 이모지입니다."));
        return EmojiDetailResponse.from(emoji);
    }

}
