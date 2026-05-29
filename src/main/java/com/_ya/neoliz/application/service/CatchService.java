package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.CatchGameResult;
import com._ya.neoliz.domain.CatchGameSession;
import com._ya.neoliz.domain.CatchRound;
import com._ya.neoliz.domain.Emoji;
import com._ya.neoliz.global.exception.CatchPlayLimitExceededException;
import com._ya.neoliz.persistence.repository.CatchGameResultRepository;
import com._ya.neoliz.persistence.repository.CatchGameSessionRepository;
import com._ya.neoliz.persistence.repository.EmojiRepository;
import com._ya.neoliz.presentation.dto.response.CatchStatusResponse;
import com._ya.neoliz.presentation.dto.response.StartCatchGameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 이모지 캐치 도메인 Service
 *
 * - 일일 플레이 가능 횟수 조회 / 게임 시작 / 결과 제출 비즈니스 로직 담당
 * - 트랜잭션 클래스 레벨 @Transactional(readOnly = true) → 조회 메서드 최적화
 *   쓰기가 필요한 메서드는 메서드 단위로 @Transactional 따로 붙일 것
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatchService {

    /** 한국 표준시 (모든 일일 카운트의 자정 기준) */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 일일 최대 플레이 횟수 (명세 기준 3회) */
    private static final int MAX_PLAYS = 3;

    /** 한 게임의 총 라운드 수 */
    private static final int ROUND_COUNT = 10;

    /** 게임 세션 만료 시간 (분) */
    private static final int SESSION_EXPIRE_MINUTES = 5;

    // ─── 라운드 난이도 공식 상수 (center 0.5 기준 반폭이 라운드마다 좁아짐) ───
    /** PERFECT 존 1라운드 반폭 (0.43~0.57 → 반폭 0.07) */
    private static final double PERFECT_BASE_HALF = 0.07;
    /** PERFECT 존 라운드당 반폭 감소량 */
    private static final double PERFECT_HALF_STEP = 0.004;
    /** GOOD 존 1라운드 반폭 (0.33~0.67 → 반폭 0.17) */
    private static final double GOOD_BASE_HALF = 0.17;
    /** GOOD 존 라운드당 반폭 감소량 */
    private static final double GOOD_HALF_STEP = 0.006;
    /** 바 기본 속도 (1라운드) */
    private static final double BAR_BASE_SPEED = 1.0;
    /** 바 라운드당 속도 증가량 */
    private static final double BAR_SPEED_STEP = 0.1;

    private final CatchGameResultRepository catchGameResultRepository;
    private final CatchGameSessionRepository catchGameSessionRepository;
    private final EmojiRepository emojiRepository;

    /**
     * 오늘 플레이 가능 횟수 조회
     *
     * 처리 흐름:
     *   1) 오늘 날짜를 KST 기준으로 구함
     *   2) 오늘 00:00:00 ~ 23:59:59.999999999 범위에서 사용자의 게임 결과 카운트
     *   3) remainingPlays = MAX_PLAYS - 사용 횟수 (최소 0)
     *   4) isPlayable = (remainingPlays > 0)
     *
     * @param userId JWT에서 추출한 사용자 PK
     * @return 오늘 플레이 가능 여부 + 남은 횟수 + 최대 횟수
     */
    public CatchStatusResponse getStatus(Long userId) {
        int remainingPlays = Math.max(0, MAX_PLAYS - countTodayPlays(userId));
        return CatchStatusResponse.of(remainingPlays, MAX_PLAYS);
    }

    /**
     * 게임 시작 — 잔여 횟수 차감 + 세션 발급 + 10라운드 데이터 반환
     *
     * 처리 흐름:
     *   1) 오늘 사용 횟수 검증 (잔여 0 → CatchPlayLimitExceededException 403)
     *   2) 이모지 풀에서 랜덤 10개 선정 (중복 없음)
     *   3) 라운드별 난이도 계산하여 라운드 데이터 생성
     *   4) gameId(UUID) 발급 + 세션(PLAYING) 저장
     *   5) 잔여 횟수 차감 (catch_game_results 에 게임 시작 기록 INSERT)
     *   6) gameId + totalRounds + rounds 반환
     *
     * @param userId JWT에서 추출한 사용자 PK
     * @return 게임 시작 응답 (gameId, totalRounds, rounds)
     */
    @Transactional   // 쓰기 작업이라 readOnly 덮어쓰기
    public StartCatchGameResponse startGame(Long userId) {
        // (1) 잔여 횟수 검증
        if (countTodayPlays(userId) >= MAX_PLAYS) {
            throw new CatchPlayLimitExceededException("오늘 플레이 횟수를 모두 사용했습니다.");
        }

        // (2) 이모지 랜덤 선정
        List<Emoji> emojis = emojiRepository.findRandomEmojis(ROUND_COUNT);

        // (3) 라운드 데이터 생성
        List<CatchRound> rounds = buildRounds(emojis);

        // (4) 세션 생성 + 저장
        String gameId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(KST).plusMinutes(SESSION_EXPIRE_MINUTES);
        catchGameSessionRepository.save(
                CatchGameSession.createNew(gameId, userId, rounds, expiresAt)
        );

        // (5) 잔여 횟수 차감 (게임 시작 시점에 기록 → 중간 이탈해도 사용 처리)
        catchGameResultRepository.save(
                CatchGameResult.builder()
                        .userId(userId)
                        .playedAt(LocalDateTime.now(KST))
                        .build()
        );

        // (6) 응답 반환
        return StartCatchGameResponse.of(gameId, rounds);
    }

    // ───────────────────────────────────────────────
    //  내부 헬퍼 메서드
    // ───────────────────────────────────────────────

    /**
     * 오늘(KST 자정 기준) 사용자가 플레이한 횟수 카운트.
     * 범위: 오늘 00:00:00.000000000 ~ 23:59:59.999999999
     */
    private int countTodayPlays(Long userId) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return catchGameResultRepository.countByUserIdAndPlayedAtBetween(userId, startOfDay, endOfDay);
    }

    /**
     * 선정된 이모지 목록으로 라운드 데이터를 생성.
     * - 라운드가 진행될수록 PERFECT/GOOD 존이 좁아지고 바 속도가 빨라짐
     * - 모든 존은 center 0.5 기준 대칭
     */
    private List<CatchRound> buildRounds(List<Emoji> emojis) {
        List<CatchRound> rounds = new ArrayList<>();
        for (int i = 0; i < emojis.size(); i++) {
            Emoji emoji = emojis.get(i);
            int roundNo = i + 1;

            double perfectHalf = PERFECT_BASE_HALF - PERFECT_HALF_STEP * (roundNo - 1);
            double goodHalf = GOOD_BASE_HALF - GOOD_HALF_STEP * (roundNo - 1);
            double barSpeed = BAR_BASE_SPEED + BAR_SPEED_STEP * (roundNo - 1);

            CatchRound round = CatchRound.builder()
                    .round(roundNo)
                    .emojiId(emoji.getId())
                    .imageUrl(emoji.getImageUrl())
                    .soundUrl(emoji.getSoundUrl())
                    .perfectZone(zoneOf(perfectHalf))
                    .goodZone(zoneOf(goodHalf))
                    .barSpeed(round3(barSpeed))
                    .build();
            rounds.add(round);
        }
        return rounds;
    }

    /** center 0.5 기준 ±half 영역 생성 (소수 3자리 반올림) */
    private CatchRound.Zone zoneOf(double half) {
        return CatchRound.Zone.builder()
                .start(round3(0.5 - half))
                .end(round3(0.5 + half))
                .build();
    }

    /** 소수점 3자리 반올림 (부동소수점 오차 방지) */
    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
