package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.Sequence;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class SequencePageResponse {
    private Long totalCount;
    private int currentPage;
    private int totalPages;
    private List<SequenceSummaryResponse> sequences;

    public static SequencePageResponse from(Page<Sequence> page) {
        return SequencePageResponse.builder()
                .totalCount(page.getTotalElements())
                .currentPage(page.getNumber()+1)
                .totalPages(page.getTotalPages())
                .sequences(page.getContent().stream()
                        .map(SequenceSummaryResponse::from)
                        .toList())
                .build();
    }
}
