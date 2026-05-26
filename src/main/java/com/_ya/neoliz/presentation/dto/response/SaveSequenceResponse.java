package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.Sequence;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SaveSequenceResponse {

    private Long id;
    private String title;
    private LocalDateTime savedAt;

    public static SaveSequenceResponse from(Sequence sequence) {
        return SaveSequenceResponse.builder()
                .id(sequence.getId())
                .title(sequence.getTitle())
                .savedAt(sequence.getCreatedAt())
                .build();
    }

}
