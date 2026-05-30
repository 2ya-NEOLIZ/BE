package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 이모지 캐치 게임 전용 주간 랭킹 응답 DTO
 *
 * - 이번 주(월~일, KST) 사용자별 캐치 게임 "최고 점수" 기준 TOP5
 * - 글로벌 주간 랭킹(ScoreLog 합산)과는 별개의 랭킹
 * - 본인이 TOP5 안에 있으면 top5의 해당 항목 isMe=true, me는 null
 *   TOP5 밖이면 me에 본인 정보(rank, nickname, profileImageUrl, score)
 */
@Getter
@AllArgsConstructor
@Builder
public class CatchRankingResponse {

    /** 주간 캐치 최고점 TOP5 */
    private List<RankEntry> top5;

    /** 본인 순위 (TOP5 밖일 때만, 안에 있으면 null) */
    private RankEntry me;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class RankEntry {
        /** 순위 */
        private Integer rank;
        /** 닉네임 */
        private String nickname;
        /** 프로필 이미지 URL */
        private String profileImageUrl;
        /** 점수 (해당 사용자의 주간 캐치 최고점) */
        private Integer score;
        /** 본인 여부 (TOP5 항목에서만 사용, me 객체에서는 null) */
        private Boolean isMe;
    }
}
