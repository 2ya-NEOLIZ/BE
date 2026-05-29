package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class WeeklyRankingResponse {
    private LocalDateTime weekStart;
    private LocalDateTime weekEnd;
    private List<RankEntry> top5;
    private RankEntry me;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class RankEntry {
        private Integer rank;
        private String nickname;
        private String profileImageUrl;
        private Integer totalScore;
        private Boolean isMe;
    }
}
