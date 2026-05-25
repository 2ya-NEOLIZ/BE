package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.Emoji;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmojiDetailResponse {
    private Long id;
    private String name;
    private String category;
    private String imageUrl;
    private String soundUrl;
    private String description;

    public static EmojiDetailResponse from(Emoji emoji) {
        return EmojiDetailResponse.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .category(emoji.getCategory().name())
                .imageUrl(emoji.getImageUrl())
                .soundUrl(emoji.getSoundUrl())
                .description(emoji.getDescription())
                .build();
    }
}
