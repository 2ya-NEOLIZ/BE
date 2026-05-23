package com._ya.neoliz.global.exception;

/**
 * 퀴즈 도메인에서 퀴즈/스케줄/시퀀스 등을 DB에서 찾지 못했을 때 발생시키는 커스텀 예외
 * - GlobalExceptionHandler 에서 잡아 ApiResponse 포맷의 500 응답으로 변환
 * - 데이터 정합성 문제 (오늘 출제된 퀴즈가 없거나, quiz_id 가 가리키는 퀴즈가 없음) 상황을 표현
 */
public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException(String message) {
        super(message);
    }
}
