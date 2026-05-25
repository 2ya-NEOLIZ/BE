package com._ya.neoliz.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EmojiListResponse {
    private String category;
    private List<EmojiResponse> emojis;

    public static EmojiListResponse of(String category, List<EmojiResponse> emojis) {
        return EmojiListResponse.builder()
                .category(category)
                .emojis(emojis)
                .build();
    }
}
