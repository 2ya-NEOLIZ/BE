package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.CatchGameResult;
import com._ya.neoliz.domain.CatchGameSession;
import com._ya.neoliz.domain.CatchGameStatus;
import com._ya.neoliz.domain.CatchRound;
import com._ya.neoliz.domain.Emoji;
import com._ya.neoliz.domain.Judgment;
import com._ya.neoliz.domain.ScoreLog;
import com._ya.neoliz.domain.ScoreType;
import com._ya.neoliz.domain.User;
import com._ya.neoliz.global.exception.CatchGameSessionInvalidException;
import com._ya.neoliz.global.exception.CatchPlayLimitExceededException;
import com._ya.neoliz.global.exception.CatchResultAlreadySubmittedException;
import com._ya.neoliz.global.exception.CatchScoreValidationException;
import com._ya.neoliz.global.exception.ForbiddenException;
import com._ya.neoliz.global.exception.UserNotFoundException;
import com._ya.neoliz.persistence.repository.CatchGameResultRepository;
import com._ya.neoliz.persistence.repository.CatchGameSessionRepository;
import com._ya.neoliz.persistence.repository.EmojiRepository;
import com._ya.neoliz.persistence.repository.ScoreLogRepository;
import com._ya.neoliz.persistence.repository.UserRepository;
import com._ya.neoliz.presentation.dto.request.SubmitCatchResultRequest;
import com._ya.neoliz.presentation.dto.request.SubmitCatchResultRequest.RoundResult;
import com._ya.neoliz.presentation.dto.response.CatchRankingResponse;
import com._ya.neoliz.presentation.dto.response.CatchRankingResponse.RankEntry;
import com._ya.neoliz.presentation.dto.response.CatchStatusResponse;
import com._ya.neoliz.presentation.dto.response.StartCatchGameResponse;
import com._ya.neoliz.presentation.dto.response.SubmitCatchResultResponse;
import com._ya.neoliz.presentation.dto.response.SubmitCatchResultResponse.MyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
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

    /** 라운드 최대 점수 (PERFECT) — 점수 검증 상한 */
    private static final int MAX_ROUND_SCORE = Judgment.PERFECT.getScore();

    /** 콤보 보너스 단가 (최대 콤보 1당 가산 점수) */
    private static final int COMBO_BONUS_UNIT = 5;

    /** 라운드 점수 이론상 최대 (10라운드 × PERFECT) */
    private static final int MAX_ROUND_TOTAL = ROUND_COUNT * MAX_ROUND_SCORE;

    /** 게임 이론상 최대 점수 (라운드 최대 + 콤보 보너스 최대) */
    private static final int MAX_TOTAL_SCORE = MAX_ROUND_TOTAL + ROUND_COUNT * COMBO_BONUS_UNIT;

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

    /** 캐치 주간 랭킹 노출 인원 (TOP5) */
    private static final int RANKING_TOP_N = 5;

    private final CatchGameResultRepository catchGameResultRepository;
    private final CatchGameSessionRepository catchGameSessionRepository;
    private final EmojiRepository emojiRepository;
    private final ScoreLogRepository scoreLogRepository;
    private final UserRepository userRepository;

    /**
     * 오늘 플레이 가능 횟수 조회
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
     *   0) 사용자 row에 비관적 락 → 동일 유저의 동시 요청 직렬화 (체크-후-삽입 원자성 보장)
     *   1) 오늘 사용 횟수 검증 (잔여 0 → CatchPlayLimitExceededException 403)
     *   2) 이모지 풀에서 랜덤 10개 선정 (중복 없음)
     *   3) 라운드별 난이도 계산하여 라운드 데이터 생성
     *   4) gameId(UUID) 발급 + 세션(PLAYING) 저장
     *   5) 잔여 횟수 차감 (catch_game_results 에 게임 시작 기록 INSERT, gameId 연결)
     *   6) gameId + totalRounds + rounds 반환
     *
     * @param userId JWT에서 추출한 사용자 PK
     * @return 게임 시작 응답 (gameId, totalRounds, rounds)
     */
    @Transactional   // 쓰기 작업이라 readOnly 덮어쓰기
    public StartCatchGameResponse startGame(Long userId) {
        // (0) 동시 요청 직렬화 — 같은 유저의 중복 시작 요청이 카운트 체크를 동시에 통과하지 못하도록 락
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        // (1) 잔여 횟수 검증
        if (countTodayPlays(userId) >= MAX_PLAYS) {
            throw new CatchPlayLimitExceededException("오늘 플레이 횟수를 모두 사용했습니다.");
        }

        // (2) 이모지 랜덤 선정 + (3) 라운드 데이터 생성
        List<Emoji> emojis = emojiRepository.findRandomEmojis(ROUND_COUNT);
        List<CatchRound> rounds = buildRounds(emojis);

        // (4) 세션 생성 + 저장
        String gameId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(KST).plusMinutes(SESSION_EXPIRE_MINUTES);
        catchGameSessionRepository.save(
                CatchGameSession.createNew(gameId, userId, rounds, expiresAt)
        );

        // (5) 잔여 횟수 차감 — 게임 시작 시점에 기록(gameId 연결), 결과 제출 시 점수 채움
        catchGameResultRepository.save(
                CatchGameResult.start(userId, gameId, LocalDateTime.now(KST))
        );

        // (6) 응답 반환
        return StartCatchGameResponse.of(gameId, rounds);
    }

    /**
     * 게임 결과 제출 — 점수 검증 + 결과 저장 + 신기록 판단 + 주간 랭킹 반환
     *
     * 처리 흐름:
     *   1) gameId로 세션 조회 (없음/만료 → 400, 타인 → 403, 이미 제출 → 409)
     *   2) abandoned=true → 0점 처리 + 랭킹 미반영 + 세션 ABANDONED
     *   3) abandoned=false → 점수 검증 → 결과 저장 + ScoreLog 적립(글로벌 랭킹 반영) + 세션 FINISHED
     *   4) 신기록 판단(isPersonalBest, previousBestScore, isInRanking)
     *   5) 캐치 전용 주간 랭킹(사용자별 최고점) 조회 후 응답 조립
     *
     * @param userId  JWT에서 추출한 사용자 PK
     * @param request 게임 결과 제출 요청
     * @return 정상/비정상 종료 응답
     */
    @Transactional
    public SubmitCatchResultResponse submitResult(Long userId, SubmitCatchResultRequest request) {
        // (1) 세션 검증
        CatchGameSession session = catchGameSessionRepository.findById(request.getGameId())
                .orElseThrow(() -> new CatchGameSessionInvalidException("존재하지 않는 게임 세션입니다."));

        if (session.getExpiresAt().isBefore(LocalDateTime.now(KST))) {
            throw new CatchGameSessionInvalidException("만료된 게임 세션입니다.");
        }
        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenException("본인의 게임 세션이 아닙니다.");
        }
        if (!session.isPlaying()) {
            throw new CatchResultAlreadySubmittedException("이미 결과가 제출된 게임입니다.");
        }

        // 게임 시작 시 생성된 결과 row 조회 (점수를 채워 넣을 대상)
        CatchGameResult result = catchGameResultRepository.findByGameId(request.getGameId())
                .orElseThrow(() -> new CatchGameSessionInvalidException("게임 기록을 찾을 수 없습니다."));

        // (2) 비정상 종료 분기
        if (Boolean.TRUE.equals(request.getAbandoned())) {
            result.abandon();
            session.changeStatus(CatchGameStatus.ABANDONED);
            return SubmitCatchResultResponse.abandoned();
        }

        // (3) 점수 검증
        validateScore(request);

        // 신기록 판단은 이번 결과 저장 전에 (이전 최고 점수 기준)
        Integer previousBest = catchGameResultRepository.findMaxTotalScoreByUserId(userId);
        boolean isPersonalBest = (previousBest == null) || (request.getTotalScore() > previousBest);

        // 결과 저장 + 세션 종료
        result.finish(request.getTotalScore(), request.getMaxCombo(),
                request.getPerfectCount(), request.getGoodCount(), request.getMissCount());
        session.changeStatus(CatchGameStatus.FINISHED);

        // ScoreLog 적립 → 글로벌 주간 랭킹(/ranking)에 캐치 점수 기여
        scoreLogRepository.save(ScoreLog.of(userId, ScoreType.CATCH, request.getTotalScore()));

        // (5) 캐치 전용 주간 랭킹 조회 (사용자별 최고점 기준)
        CatchRankingResponse ranking = buildCatchRanking(userId);
        boolean isInRanking = isInTop5(ranking);

        MyResult myResult = MyResult.builder()
                .totalScore(request.getTotalScore())
                .maxCombo(request.getMaxCombo())
                .perfectCount(request.getPerfectCount())
                .goodCount(request.getGoodCount())
                .missCount(request.getMissCount())
                .isPersonalBest(isPersonalBest)
                .previousBestScore(previousBest == null ? 0 : previousBest)
                .isInRanking(isInRanking)
                .remainingPlays(Math.max(0, MAX_PLAYS - countTodayPlays(userId)))
                .build();

        return SubmitCatchResultResponse.of(myResult, ranking);
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
     * 제출된 결과의 점수 검증.
     * - results 개수가 라운드 수와 일치
     * - 각 라운드 판정/점수 유효성 (judgment != null, 0 ≤ score ≤ 50)
     * - maxCombo 0~10 범위
     * - results 점수 합 + 콤보 보너스(maxCombo × 5) = totalScore
     * - 판정별 횟수(perfect/good/miss) = 실제 results 판정 분포
     * - totalScore 이론상 최대(550 = 라운드 500 + 콤보 50) 이내
     */
    private void validateScore(SubmitCatchResultRequest req) {
        List<RoundResult> results = req.getResults();
        if (results == null || results.size() != ROUND_COUNT) {
            throw new CatchScoreValidationException("라운드 결과 개수가 올바르지 않습니다.");
        }
        if (req.getTotalScore() == null || req.getMaxCombo() == null
                || req.getPerfectCount() == null || req.getGoodCount() == null || req.getMissCount() == null) {
            throw new CatchScoreValidationException("점수 관련 필드가 누락되었습니다.");
        }

        // maxCombo 범위 먼저 검증 (콤보 보너스 계산에 사용되므로)
        if (req.getMaxCombo() < 0 || req.getMaxCombo() > ROUND_COUNT) {
            throw new CatchScoreValidationException("최대 콤보 범위를 벗어났습니다.");
        }

        int sum = 0, perfect = 0, good = 0, miss = 0;
        for (RoundResult r : results) {
            if (r.getJudgment() == null || r.getScore() == null) {
                throw new CatchScoreValidationException("라운드 판정/점수가 누락되었습니다.");
            }
            if (r.getScore() < 0 || r.getScore() > MAX_ROUND_SCORE) {
                throw new CatchScoreValidationException("라운드 점수 범위를 벗어났습니다.");
            }
            sum += r.getScore();
            switch (r.getJudgment()) {
                case PERFECT -> perfect++;
                case GOOD -> good++;
                case MISS -> miss++;
            }
        }

        // 라운드 점수 합 + 콤보 보너스(maxCombo × 5) 가 totalScore 와 일치해야 함
        int comboBonus = req.getMaxCombo() * COMBO_BONUS_UNIT;
        if (sum + comboBonus != req.getTotalScore()) {
            throw new CatchScoreValidationException("라운드 점수 합과 콤보 보너스의 합이 총 점수와 일치하지 않습니다.");
        }
        if (req.getTotalScore() < 0 || req.getTotalScore() > MAX_TOTAL_SCORE) {
            throw new CatchScoreValidationException("총 점수가 허용 범위를 벗어났습니다.");
        }
        if (perfect != req.getPerfectCount() || good != req.getGoodCount() || miss != req.getMissCount()) {
            throw new CatchScoreValidationException("판정 횟수가 라운드 결과와 일치하지 않습니다.");
        }
    }

    /**
     * 이번 주(월~일, KST) 캐치 게임 사용자별 최고점 기준 주간 랭킹 조립.
     * - TOP5 구성 + 본인 순위 계산
     * - 본인이 TOP5 안: 해당 항목 isMe=true, me=null
     * - 본인이 TOP5 밖: me 에 본인 정보(rank/nickname/profileImageUrl/score)
     */
    private CatchRankingResponse buildCatchRanking(Long userId) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime weekEnd = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

        List<Object[]> rows = catchGameResultRepository.findWeeklyBestScores(weekStart, weekEnd);

        List<RankEntry> top5 = new ArrayList<>();
        RankEntry me = null;
        boolean meInTop5 = false;

        for (int i = 0; i < rows.size(); i++) {
            Long rankUserId = ((Number) rows.get(i)[0]).longValue();
            int bestScore = ((Number) rows.get(i)[1]).intValue();
            int rank = i + 1;
            boolean isMeFlag = rankUserId.equals(userId);

            if (rank <= RANKING_TOP_N) {
                User user = userRepository.findById(rankUserId).orElse(null);
                if (user == null) continue;
                top5.add(RankEntry.builder()
                        .rank(rank)
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .score(bestScore)
                        .isMe(isMeFlag)
                        .build());
                if (isMeFlag) meInTop5 = true;
            } else if (isMeFlag) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    me = RankEntry.builder()
                            .rank(rank)
                            .nickname(user.getNickname())
                            .profileImageUrl(user.getProfileImageUrl())
                            .score(bestScore)
                            .isMe(null)
                            .build();
                }
            }
        }

        return CatchRankingResponse.builder()
                .top5(top5)
                .me(meInTop5 ? null : me)
                .build();
    }

    /** 캐치 주간 랭킹 TOP5 안에 본인이 포함되어 있는지 */
    private boolean isInTop5(CatchRankingResponse ranking) {
        if (ranking.getTop5() == null) return false;
        return ranking.getTop5().stream()
                .anyMatch(entry -> Boolean.TRUE.equals(entry.getIsMe()));
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
