package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.User;
import com._ya.neoliz.global.exception.DuplicateEmailException;
import com._ya.neoliz.global.exception.DuplicateNicknameException;
import com._ya.neoliz.persistence.repository.UserRepository;
import com._ya.neoliz.presentation.dto.request.SignupRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl("")
                .build();

        userRepository.save(user);
    }
}
