package com._ya.neoliz.global.exception;

/**
 * 이미 결과가 제출된 게임 세션(gameId)에 다시 제출을 시도했을 때 발생시키는 커스텀 예외
 * - GlobalExceptionHandler에서 잡아 ApiResponse 포맷의 409 Conflict 응답으로 변환
 */
public class CatchResultAlreadySubmittedException extends RuntimeException {
    public CatchResultAlreadySubmittedException(String message) {
        super(message);
    }
}
