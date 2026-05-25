package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.Emoji;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmojiResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String soundUrl;

    public static EmojiResponse from(Emoji emoji) {
        return EmojiResponse.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .imageUrl(emoji.getImageUrl())
                .soundUrl(emoji.getSoundUrl())
                .build();
    }
}
