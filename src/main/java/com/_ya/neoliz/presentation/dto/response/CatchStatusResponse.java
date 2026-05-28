package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 이모지 캐치 오늘 플레이 가능 횟수 조회 API 응답 DTO
 *
 * 응답 예시:
 *   - 플레이 가능: { "isPlayable": true,  "remainingPlays": 3, "maxPlays": 3 }
 *   - 횟수 소진:   { "isPlayable": false, "remainingPlays": 0, "maxPlays": 3 }
 */
@Getter
@AllArgsConstructor
@Builder
public class CatchStatusResponse {

    /** 오늘 플레이 가능 여부 (remainingPlays > 0) */
    private Boolean isPlayable;

    /** 오늘 남은 플레이 횟수 */
    private Integer remainingPlays;

    /** 일일 최대 플레이 횟수 */
    private Integer maxPlays;

    /**
     * 남은 횟수와 최대 횟수만 받아서 응답 조립.
     * isPlayable 은 remainingPlays > 0 으로 자동 계산.
     */
    public static CatchStatusResponse of(int remainingPlays, int maxPlays) {
        return CatchStatusResponse.builder()
                .isPlayable(remainingPlays > 0)
                .remainingPlays(remainingPlays)
                .maxPlays(maxPlays)
                .build();
    }
}
