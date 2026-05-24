package com._ya.neoliz.global.exception;

import com._ya.neoliz.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException unf) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("F404", unf.getMessage(), null));
    }

    /**
     * 퀴즈 도메인에서 데이터를 못 찾았을 때 발생하는 예외 핸들러
     * - 운영자가 등록한 오늘의 퀴즈가 없거나, 스케줄에 매핑된 퀴즈가 사라진 경우 등
     *   서버 측 데이터 정합성 문제에 해당하므로 500 상태로 응답
     * - 반환 타입은 Swagger 응답 스키마가 정확하게 표시되도록 명시적으로 지정
     */
    @ExceptionHandler(QuizNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleQuizNotFoundException(QuizNotFoundException qnf) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("F500", qnf.getMessage(), null));
    }
}
