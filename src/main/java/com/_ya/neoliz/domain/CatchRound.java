package com._ya.neoliz.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이모지 캐치 한 라운드의 데이터 (도메인 값객체)
 *
 * - CatchGameSession 의 rounds 필드에 JSON으로 직렬화되어 저장됨
 * - 게임 시작 응답으로 프론트에 그대로 전달되어, 프론트가 매 라운드 판정에 사용
 * - 결과 제출 API에서 서버 채점 검증 시 다시 읽어 사용 예정
 *
 * Jackson 직렬화/역직렬화(@JdbcTypeCode(SqlTypes.JSON))를 위해
 * 기본 생성자 + 세터를 둔다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatchRound {

    /** 라운드 번호 (1 ~ 10) */
    private int round;

    /** 출제 이모지 PK */
    private Long emojiId;

    /** 이모지 이미지 URL */
    private String imageUrl;

    /** 이모지 사운드 URL */
    private String soundUrl;

    /** PERFECT 판정 영역 (0~1 비율) */
    private Zone perfectZone;

    /** GOOD 판정 영역 (perfectZone 포함, 더 넓음) */
    private Zone goodZone;

    /** 바 이동 속도 (라운드 진행될수록 빨라짐) */
    private double barSpeed;

    /**
     * 판정 영역 (시작점 ~ 끝점, 0~1 비율)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Zone {
        private double start;
        private double end;
    }
}
