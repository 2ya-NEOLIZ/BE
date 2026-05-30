package com._ya.neoliz.global.exception;

/**
 * 게임 세션(gameId)이 존재하지 않거나 만료된 경우 발생시키는 커스텀 예외
 * - GlobalExceptionHandler에서 잡아 ApiResponse 포맷의 400 Bad Request 응답으로 변환
 */
public class CatchGameSessionInvalidException extends RuntimeException {
    public CatchGameSessionInvalidException(String message) {
        super(message);
    }
}
