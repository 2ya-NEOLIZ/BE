package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 데일리 퀴즈 힌트 조회 API 응답 DTO
 *
 * 정답의 카테고리 정보를 반환한다.
 * 카테고리는 "동물", "영화", "노래", "밈" 중 하나.
 */
@Getter
@AllArgsConstructor
@Builder
public class UseHintResponse {

    private String category;
    private String hint;

    public static UseHintResponse of(String category, String hint) {
        return UseHintResponse.builder()
                .category(category)
                .hint(hint)
                .build();
    }
}
