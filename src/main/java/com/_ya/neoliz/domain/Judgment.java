package com._ya.neoliz.domain;

/**
 * 이모지 캐치 라운드 판정 결과
 * - PERFECT : 정확히 맞춤 (라운드당 50점)
 * - GOOD    : 근접 (라운드당 30점)
 * - MISS    : 실패 (0점)
 */
public enum Judgment {
    PERFECT(50),
    GOOD(30),
    MISS(0);

    private final int score;

    Judgment(int score) {
        this.score = score;
    }

    /** 해당 판정의 정상 점수 (서버 점수 검증용) */
    public int getScore() {
        return score;
    }
}
