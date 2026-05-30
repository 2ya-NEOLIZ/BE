package com._ya.neoliz.domain;

/**
 * 이모지 캐치 게임 세션 상태
 * - PLAYING   : 게임 진행 중 (시작 직후)
 * - FINISHED  : 정상 종료 (결과 제출 완료)
 * - ABANDONED : 중간 이탈
 * - EXPIRED   : 만료 시간 초과
 */
public enum CatchGameStatus {
    PLAYING,
    FINISHED,
    ABANDONED,
    EXPIRED
}
