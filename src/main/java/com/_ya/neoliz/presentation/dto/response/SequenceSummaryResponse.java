package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.Sequence;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SequenceSummaryResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;

    public static SequenceSummaryResponse from(Sequence sequence) {
        return SequenceSummaryResponse.builder()
                .id(sequence.getId())
                .title(sequence.getTitle())
                .createdAt(sequence.getCreatedAt())
                .build();
    }
}
