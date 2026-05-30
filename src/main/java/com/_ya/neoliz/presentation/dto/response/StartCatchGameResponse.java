package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.CatchRound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 이모지 캐치 게임 시작 API 응답 DTO
 *
 * gameId + 전체 라운드 수 + 10라운드 데이터를 한 번에 반환한다.
 * (프론트가 매 라운드 서버 호출 없이 클라이언트에서 판정하도록 전체 데이터 일괄 전달)
 */
@Getter
@AllArgsConstructor
@Builder
public class StartCatchGameResponse {

    /** 게임 세션 식별자 (UUID) */
    private String gameId;

    /** 전체 라운드 수 */
    private int totalRounds;

    /** 라운드별 데이터 (도메인 값객체 그대로 노출) */
    private List<CatchRound> rounds;

    public static StartCatchGameResponse of(String gameId, List<CatchRound> rounds) {
        return StartCatchGameResponse.builder()
                .gameId(gameId)
                .totalRounds(rounds.size())
                .rounds(rounds)
                .build();
    }
}
