package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 이모지 캐치 게임 결과 제출 API 응답 DTO
 *
 * 두 가지 형태로 분기:
 *   - 정상 종료(abandoned=false): myResult(개인 결과+신기록) + ranking(주간 캐치 TOP5)
 *   - 비정상 종료(abandoned=true): score=0 + ranking=null
 *
 * 주간 랭킹(ranking)은 캐치 게임 전용 CatchRankingResponse (사용자별 최고점 기준).
 */
@Getter
@AllArgsConstructor
@Builder
public class SubmitCatchResultResponse {

    /** 개인 결과 + 신기록 정보 (정상 종료 시) */
    private MyResult myResult;

    /** 주간 캐치 랭킹 TOP5 + 본인 순위 (정상 종료 시) */
    private CatchRankingResponse ranking;

    /** 비정상 종료 시 점수 (0). 정상 종료 시 null */
    private Integer score;

    /** 정상 종료 응답 */
    public static SubmitCatchResultResponse of(MyResult myResult, CatchRankingResponse ranking) {
        return SubmitCatchResultResponse.builder()
                .myResult(myResult)
                .ranking(ranking)
                .build();
    }

    /** 비정상 종료 응답 (score=0, ranking=null) */
    public static SubmitCatchResultResponse abandoned() {
        return SubmitCatchResultResponse.builder()
                .score(0)
                .build();
    }

    /**
     * 개인 결과 + 신기록 정보
     */
    @Getter
    @AllArgsConstructor
    @Builder
    public static class MyResult {
        /** 총 점수 */
        private Integer totalScore;
        /** 최대 콤보 */
        private Integer maxCombo;
        /** PERFECT 횟수 */
        private Integer perfectCount;
        /** GOOD 횟수 */
        private Integer goodCount;
        /** MISS 횟수 */
        private Integer missCount;
        /** 개인 신기록 여부 */
        private Boolean isPersonalBest;
        /** 이전 최고 점수 */
        private Integer previousBestScore;
        /** 주간 랭킹(TOP5) 진입 여부 */
        private Boolean isInRanking;
        /** 남은 플레이 횟수 */
        private Integer remainingPlays;
    }
}
