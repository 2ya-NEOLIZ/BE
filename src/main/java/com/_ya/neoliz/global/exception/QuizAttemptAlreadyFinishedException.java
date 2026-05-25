package com._ya.neoliz.global.exception;

/**
 * 이미 종료된 퀴즈에 또 제출/포기를 시도했을 때 발생시키는 커스텀 예외
 * - 정답을 맞췄거나, 5회 시도를 모두 사용했거나, 이미 포기한 퀴즈가 대상
 * - GlobalExceptionHandler에서 잡아 ApiResponse 포맷의 409 Conflict 응답으로 변환
 */
public class QuizAttemptAlreadyFinishedException extends RuntimeException {
    public QuizAttemptAlreadyFinishedException(String message) {
        super(message);
    }
}
