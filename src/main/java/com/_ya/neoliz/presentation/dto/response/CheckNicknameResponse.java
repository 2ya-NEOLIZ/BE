package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckNicknameResponse {
    private Boolean available;

    public static CheckNicknameResponse of(boolean available) {
        return new CheckNicknameResponse(available);
    }
}
