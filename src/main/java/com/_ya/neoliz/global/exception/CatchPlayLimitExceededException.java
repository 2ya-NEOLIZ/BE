package com._ya.neoliz.global.exception;

/**
 * 오늘 이모지 캐치 플레이 횟수를 모두 소진한 상태에서 게임 시작을 요청했을 때 발생시키는 커스텀 예외
 * - 일일 최대 3회
 * - GlobalExceptionHandler에서 잡아 ApiResponse 포맷의 403 Forbidden 응답으로 변환
 */
public class CatchPlayLimitExceededException extends RuntimeException {
    public CatchPlayLimitExceededException(String message) {
        super(message);
    }
}
