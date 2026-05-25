package com._ya.neoliz.presentation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$")
    private String password;

    @NotBlank
    private String passwordConfirm;

    @NotBlank
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$")
    private String nickname;

    @AssertTrue(message = "비밀번호가 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirm);
    }
}
