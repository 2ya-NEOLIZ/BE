package com._ya.neoliz.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckEmailResponse {
    private Boolean available;

    public static CheckEmailResponse of(boolean available) {
        return new CheckEmailResponse(available);
    }
}
