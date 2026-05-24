package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.SequenceItems;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SequenceItemsResponse {
    private Long emojiId;
    private BigDecimal multiplier;
    private String soundUrl;
    private String imageUrl;

    public static SequenceItemsResponse from(SequenceItems sequenceItems, String soundUrl, String imageUrl) {
        return SequenceItemsResponse.builder()
                .emojiId(sequenceItems.getEmojiId())
                .multiplier(sequenceItems.getMultiplier())
                .soundUrl(soundUrl)
                .imageUrl(imageUrl)
                .build();
    }
}
