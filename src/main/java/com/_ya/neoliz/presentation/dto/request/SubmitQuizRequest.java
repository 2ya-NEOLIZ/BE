package com._ya.neoliz.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 데일리 퀴즈 정답 제출 API 요청 DTO
 *
 * - answer    : 사용자가 입력한 정답 텍스트
 *               (포기한 경우에는 비교에 사용되지 않으므로 빈 문자열이어도 OK)
 * - isGivenUp : 포기 버튼 클릭 여부 (true면 정답 비교 없이 포기 처리)
 *
 * Jackson은 @NoArgsConstructor + setter/필드를 통해 JSON을 매핑하므로
 * 빌더 패턴이 따로 필요 없음.
 */
@Getter
@NoArgsConstructor                                          // JSON → 객체 역직렬화 시 필요
@AllArgsConstructor                                         // 테스트용 전체 필드 생성자
public class SubmitQuizRequest {

    /** 사용자가 입력한 정답 텍스트 (포기 시 빈 문자열 허용) */
    private String answer;

    /** 포기 여부 (true면 정답 비교 안 하고 포기 처리, null 방지 위해 @NotNull) */
    @NotNull(message = "isGivenUp 값은 필수입니다.")
    private Boolean isGivenUp;
}
