package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.Sequence;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SequenceDetailResponse {
    private Long id;
    private String title;
    private List<SequenceItemResponse> items;

    public static SequenceDetailResponse from(Sequence sequence, List<SequenceItemResponse> itemsList) {
        return SequenceDetailResponse.builder()
                .id(sequence.getId())
                .title(sequence.getTitle())
                .items(itemsList)
                .build();
    }

}
