package com._ya.neoliz.presentation.dto.request;

import com._ya.neoliz.domain.Judgment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 이모지 캐치 게임 결과 제출 API 요청 DTO
 *
 * - abandoned=true 인 경우 점수 관련 필드는 무시되고 0점/랭킹 미반영으로 처리됨
 * - abandoned=false(정상 종료) 인 경우 results 합계/통계가 서버에서 검증됨
 */
@Getter
@NoArgsConstructor
public class SubmitCatchResultRequest {

    /** 게임 세션 식별자 (UUID) */
    @NotBlank(message = "gameId는 필수입니다.")
    private String gameId;

    /** 라운드별 결과 (정상 종료 시 10개) */
    private List<RoundResult> results;

    /** 총 점수 */
    private Integer totalScore;

    /** 최대 콤보 (0 ~ 10) */
    private Integer maxCombo;

    /** PERFECT 판정 횟수 */
    private Integer perfectCount;

    /** GOOD 판정 횟수 */
    private Integer goodCount;

    /** MISS 판정 횟수 */
    private Integer missCount;

    /** 게임 중단 여부 (기본 false, true면 점수 데이터 무시) */
    @NotNull(message = "abandoned 값은 필수입니다.")
    private Boolean abandoned;

    /**
     * 라운드 1개의 결과
     */
    @Getter
    @NoArgsConstructor
    public static class RoundResult {
        /** 라운드 번호 */
        private Integer round;
        /** 판정 (PERFECT / GOOD / MISS) */
        private Judgment judgment;
        /** 해당 라운드 획득 점수 */
        private Integer score;
    }
}
