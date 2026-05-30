package com._ya.neoliz.global.exception;

/**
 * 제출된 게임 결과의 점수 검증에 실패했을 때 발생시키는 커스텀 예외
 * - results 점수 합 ≠ totalScore, 잘못된 judgment, 이론상 최대 점수 초과, maxCombo 범위 초과 등
 * - GlobalExceptionHandler에서 잡아 ApiResponse 포맷의 400 Bad Request 응답으로 변환
 */
public class CatchScoreValidationException extends RuntimeException {
    public CatchScoreValidationException(String message) {
        super(message);
    }
}
