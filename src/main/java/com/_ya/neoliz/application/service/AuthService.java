package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.RefreshToken;
import com._ya.neoliz.domain.User;
import com._ya.neoliz.global.exception.DuplicateEmailException;
import com._ya.neoliz.global.exception.DuplicateNicknameException;
import com._ya.neoliz.global.exception.InvalidCredentialsException;
import com._ya.neoliz.global.util.JwtUtil;
import com._ya.neoliz.persistence.repository.RefreshTokenRepository;
import com._ya.neoliz.persistence.repository.UserRepository;
import com._ya.neoliz.presentation.dto.request.LoginRequest;
import com._ya.neoliz.presentation.dto.request.SignupRequest;
import com._ya.neoliz.presentation.dto.response.CheckEmailResponse;
import com._ya.neoliz.presentation.dto.response.CheckNicknameResponse;
import com._ya.neoliz.presentation.dto.response.LoginResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com._ya.neoliz.domain.ScoreLog;
import com._ya.neoliz.persistence.repository.ScoreLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com._ya.neoliz.domain.ScoreType;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final ScoreLogRepository scoreLogRepository;

    // 1. 회원 가입
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

    // 2. 이메일 중복 확인
    public CheckEmailResponse checkEmail(String email) {
        boolean available = !userRepository.existsByEmail(email);
        return CheckEmailResponse.of(available);
    }

    // 3. 닉네임 중복 확인
    public CheckNicknameResponse checkNickname(String nickname) {
        boolean available = !userRepository.existsByNickname(nickname);
        return CheckNicknameResponse.of(available);
    }

    // 4. 로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay().minusNanos(1);
        long todayLogin = scoreLogRepository.countTodayScoreByType(user.getId(), ScoreType.DAILY_LOGIN, dayStart, dayEnd);
        if (todayLogin == 0) {
            scoreLogRepository.save(ScoreLog.of(user.getId(), ScoreType.DAILY_LOGIN, 10));
        }

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder().user(user).token(refreshToken).build()
                        )
                );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .build();
    }

    // 5. 로그아웃
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

}