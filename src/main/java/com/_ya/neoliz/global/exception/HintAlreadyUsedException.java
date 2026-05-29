package com._ya.neoliz.global.exception;

/**
 * 이미 힌트를 사용한 상태에서 다시 힌트를 요청했을 때 발생시키는 커스텀 예외
 * - 힌트는 퀴즈당 1회만 사용 가능
 * - GlobalExceptionHandler에서 잡아 ApiResponse 포맷의 409 Conflict 응답으로 변환
 */
public class HintAlreadyUsedException extends RuntimeException {
    public HintAlreadyUsedException(String message) {
        super(message);
    }
}
