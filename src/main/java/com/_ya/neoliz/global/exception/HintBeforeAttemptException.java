package com._ya.neoliz.global.exception;

/**
 * 데일리 퀴즈에 1회도 시도하지 않은 상태에서 힌트를 요청했을 때 발생시키는 커스텀 예외
 * - 명세상 "1회 오답 후 사용 가능"
 * - GlobalExceptionHandler에서 잡아 ApiResponse 포맷의 403 Forbidden 응답으로 변환
 */
public class HintBeforeAttemptException extends RuntimeException {
    public HintBeforeAttemptException(String message) {
        super(message);
    }
}
