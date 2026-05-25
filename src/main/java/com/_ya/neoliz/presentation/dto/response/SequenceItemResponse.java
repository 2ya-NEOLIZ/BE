package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.SequenceItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SequenceItemResponse {
    private Long emojiId;
    private BigDecimal multiplier;
    private String soundUrl;
    private String imageUrl;

    public static SequenceItemResponse from(SequenceItem sequenceItems, String soundUrl, String imageUrl) {
        return SequenceItemResponse.builder()
                .emojiId(sequenceItems.getEmojiId())
                .multiplier(sequenceItems.getMultiplier())
                .soundUrl(soundUrl)
                .imageUrl(imageUrl)
                .build();
    }
}
