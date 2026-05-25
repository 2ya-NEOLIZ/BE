package com._ya.neoliz.global.exception;

import com._ya.neoliz.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 유저를 찾을 수 없는 경우 404
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException unf) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("F404", unf.getMessage(), null));
    }

    // 2. 퀴즈를 찾을 수 없는 경우 500 (운영자/서버 측 데이터 정합성 문제)
    @ExceptionHandler(QuizNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleQuizNotFoundException(QuizNotFoundException qnf) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("F500", qnf.getMessage(), null));
    }

    // 3.이메일 중복인 경우 409
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("F409", e.getMessage(), null));
    }

    // 4. 닉네임 중복인 경우 409
    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateNickname(DuplicateNicknameException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("F409", e.getMessage(), null));
    }

    // 5. 유효성 검사 실패 시 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("유효하지 않은 요청입니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("F400", message, null));
    }

    // 6. 이메일 또는 비밀번호가 틀린 경우 401
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", e.getMessage(), null));
    }

    // 7. 유효하지 않은 카테고리인 경우 400
    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCategoryException(InvalidCategoryException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("F400", e.getMessage(), null));
    }

    // 8. 시퀀스를 찾을 수 없는 경우 404
    @ExceptionHandler(SequenceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSequenceNotFoundException(SequenceNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("F404", e.getMessage(), null));
    }
}
