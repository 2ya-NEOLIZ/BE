package com._ya.neoliz.presentation.dto.response;

import com._ya.neoliz.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateNicknameResponse {
    private String nickname;

    public static UpdateNicknameResponse from(User user) {
        return UpdateNicknameResponse.builder()
                .nickname(user.getNickname())
                .build();
    }
}
