package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.DailyQuizSchedule;
import com._ya.neoliz.domain.QuizAttempt;
import com._ya.neoliz.domain.QuizPool;
import com._ya.neoliz.domain.QuizSequenceItem;
import com._ya.neoliz.global.exception.HintAlreadyUsedException;
import com._ya.neoliz.global.exception.HintBeforeAttemptException;
import com._ya.neoliz.global.exception.QuizAttemptAlreadyFinishedException;
import com._ya.neoliz.global.exception.QuizNotFoundException;
import com._ya.neoliz.persistence.repository.DailyQuizScheduleRepository;
import com._ya.neoliz.persistence.repository.QuizAttemptRepository;
import com._ya.neoliz.persistence.repository.QuizPoolRepository;
import com._ya.neoliz.persistence.repository.QuizSequenceItemRepository;
import com._ya.neoliz.presentation.dto.request.SubmitQuizRequest;
import com._ya.neoliz.presentation.dto.response.DailyQuizResponse;
import com._ya.neoliz.presentation.dto.response.DailyQuizResponse.EmojiInfo;
import com._ya.neoliz.presentation.dto.response.SubmitQuizResponse;
import com._ya.neoliz.presentation.dto.response.UseHintResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * 이모지 퀴즈 도메인 Service
 *
 * - 데일리 퀴즈 조회/제출/힌트 비즈니스 로직 담당
 * - 트랜잭션 클래스 레벨 @Transactional(readOnly = true) → 조회 메서드 최적화
 *   쓰기가 필요한 메서드는 메서드 단위로 @Transactional 따로 붙일 것
 */
@Service                                    // Spring이 Bean으로 등록
@RequiredArgsConstructor                    // final 필드 생성자 자동 생성 (의존성 주입)
@Transactional(readOnly = true)             // 클래스 전체를 읽기 전용 트랜잭션으로
public class QuizService {

    /** 한국 표준시 (모든 일일 카운트의 자정 기준) */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 한 퀴즈당 최대 시도 횟수 */
    private static final int MAX_ATTEMPTS = 5;

    /** 정답 기본 점수 */
    private static final int BASE_CORRECT_SCORE = 20;

    /** 힌트 사용 시 차감되는 점수 */
    private static final int HINT_PENALTY = 5;

    // ─── 의존성 (final + @RequiredArgsConstructor로 생성자 자동 주입) ───
    private final DailyQuizScheduleRepository scheduleRepository;
    private final QuizPoolRepository quizPoolRepository;
    private final QuizSequenceItemRepository quizSequenceItemRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    /**
     * 데일리 퀴즈 조회 — 오늘의 퀴즈 + 사용자 진행 상태 반환
     *
     * 처리 흐름:
     *   1) 오늘 날짜(KST 자정 기준)로 출제된 퀴즈 스케줄 찾기
     *   2) 스케줄의 quiz_id로 QuizPool 본문 조회
     *   3) 퀴즈에 속한 이모지 시퀀스를 order_index 오름차순으로 조회
     *   4) 시퀀스 → EmojiInfo DTO로 변환
     *   5) (user_id, quiz_id)로 사용자의 시도 기록 조회 (Optional, 미시도면 empty)
     *   6) DailyQuizResponse.of(...)로 case 1/2/3 분기 응답 조립
     *
     * @param userId JWT 토큰에서 추출한 사용자 PK
     * @return 데일리 퀴즈 조회 응답 DTO
     */
    public DailyQuizResponse getDailyQuiz(Long userId) {
        QuizPool quiz = getTodayQuiz();

        // 퀴즈 시퀀스 조회 (순서 보장)
        List<QuizSequenceItem> items =
                quizSequenceItemRepository.findByQuizIdOrderByOrderIndexAsc(quiz.getId());

        // TODO: 주희님의 Emoji 엔티티/EmojiRepository 머지되면 그때 imageUrl/soundUrl 채우기.
        List<EmojiInfo> sequence = items.stream()
                .map(item -> EmojiInfo.builder()
                        .emojiId(item.getEmojiId())
                        .imageUrl("")
                        .soundUrl("")
                        .build())
                .toList();

        // 사용자의 이 퀴즈 시도 기록 조회 (미시도면 Optional.empty())
        Optional<QuizAttempt> attemptOpt =
                quizAttemptRepository.findByUserIdAndQuizId(userId, quiz.getId());

        // case 1/2/3 분기 응답 조립
        return DailyQuizResponse.of(quiz, sequence, attemptOpt);
    }

    /**
     * 데일리 퀴즈 정답 제출 (또는 포기)
     *
     * 처리 흐름:
     *   1) 오늘 출제된 퀴즈 조회 (없으면 QuizNotFoundException → 500)
     *   2) 사용자 시도 기록 조회 (없으면 새로 생성)
     *   3) 이미 종료된 퀴즈인지 검증 (이미 정답/포기/5회 종료 → 409)
     *   4) 분기 처리
     *      - isGivenUp=true  → 포기 처리
     *      - isGivenUp=false → 정답 비교 + 점수 계산
     *   5) 저장 후 case별 DTO 반환
     *
     * @param userId  JWT에서 추출한 사용자 PK
     * @param request answer + isGivenUp
     * @return 4가지 case 중 하나의 SubmitQuizResponse
     */
    @Transactional   // 쓰기 작업이라 readOnly 덮어쓰기
    public SubmitQuizResponse submitQuiz(Long userId, SubmitQuizRequest request) {
        // (1) 오늘 퀴즈 조회
        QuizPool quiz = getTodayQuiz();

        // (2) 시도 기록 조회 또는 신규 생성
        QuizAttempt attempt = quizAttemptRepository.findByUserIdAndQuizId(userId, quiz.getId())
                .orElseGet(() -> QuizAttempt.createNew(userId, quiz.getId()));

        // (3) 이미 종료된 퀴즈에 다시 시도 → 409
        if (Boolean.TRUE.equals(attempt.getIsFinished())) {
            throw new QuizAttemptAlreadyFinishedException(
                    "이미 종료된 퀴즈입니다. (정답/포기/5회 소진)"
            );
        }

        // (4) 포기 분기
        if (Boolean.TRUE.equals(request.getIsGivenUp())) {
            attempt.markGivenUp();
            quizAttemptRepository.save(attempt);
            return SubmitQuizResponse.givenUp(quiz.getAnswer());
        }

        // (5) 정답 제출 분기 — 시도 횟수 증가
        attempt.incrementAttemptCount();

        boolean isCorrect = isAnswerCorrect(request.getAnswer(), quiz.getAnswer());

        if (isCorrect) {
            // case 1: 정답
            int score = calculateScore(attempt.getAttemptCount(), Boolean.TRUE.equals(attempt.getHintUsed()));
            attempt.markSolved(score);
            quizAttemptRepository.save(attempt);
            return SubmitQuizResponse.correct(attempt.getAttemptCount(), score);
        }

        // 오답 — 5회 다 썼는지 확인
        int remaining = Math.max(0, MAX_ATTEMPTS - attempt.getAttemptCount());
        if (remaining == 0) {
            // case 3: 5회째 오답 (자동 종료)
            attempt.markFailed();
            quizAttemptRepository.save(attempt);
            return SubmitQuizResponse.wrongAndFinished(quiz.getAnswer());
        }

        // case 2: 오답 (시도 가능)
        quizAttemptRepository.save(attempt);
        return SubmitQuizResponse.wrong(remaining);
    }

    /**
     * 데일리 퀴즈 힌트 사용 — 정답의 카테고리 정보 반환
     *
     * 처리 흐름:
     *   1) 오늘 출제된 퀴즈 조회 (없으면 QuizNotFoundException → 500)
     *   2) 사용자 시도 기록 조회
     *      - 시도 기록이 없거나 attemptCount=0 → HintBeforeAttemptException (403)
     *   3) 이미 종료된 퀴즈인지 검증 → QuizAttemptAlreadyFinishedException (409)
     *      - 정답/포기/5회 소진 모두 포함
     *   4) 이미 힌트를 사용했는지 검증 → HintAlreadyUsedException (409)
     *   5) attempt.useHint() 호출 + 저장 (hint_used = true)
     *   6) 카테고리 정보 반환
     *
     * @param userId JWT에서 추출한 사용자 PK
     * @return 정답 카테고리 (동물 / 영화 / 노래 / 밈)
     */
    @Transactional   // 쓰기 작업이라 readOnly 덮어쓰기
    public UseHintResponse useHint(Long userId) {
        // (1) 오늘 퀴즈 조회
        QuizPool quiz = getTodayQuiz();

        // (2) 시도 기록 조회 — 미시도 시 403
        QuizAttempt attempt = quizAttemptRepository.findByUserIdAndQuizId(userId, quiz.getId())
                .orElseThrow(() -> new HintBeforeAttemptException(
                        "1회 오답 후 사용 가능합니다."
                ));

        if (attempt.getAttemptCount() == null || attempt.getAttemptCount() < 1) {
            throw new HintBeforeAttemptException("1회 오답 후 사용 가능합니다.");
        }

        // (3) 이미 종료된 퀴즈 → 409
        if (Boolean.TRUE.equals(attempt.getIsFinished())) {
            throw new QuizAttemptAlreadyFinishedException(
                    "이미 종료된 퀴즈입니다. (정답/포기/5회 소진)"
            );
        }

        // (4) 이미 힌트 사용 → 409
        if (Boolean.TRUE.equals(attempt.getHintUsed())) {
            throw new HintAlreadyUsedException("이미 힌트를 사용한 퀴즈입니다.");
        }

        // (5) 힌트 사용 처리 + 저장
        attempt.useHint();
        quizAttemptRepository.save(attempt);

        // (6) 카테고리 반환
        return UseHintResponse.of(quiz.getCategory());
    }

    // ───────────────────────────────────────────────
    //  내부 헬퍼 메서드
    // ───────────────────────────────────────────────

    /**
     * 오늘 출제된 퀴즈를 KST 자정 기준으로 찾아서 반환.
     * - 스케줄이나 퀴즈 본문이 없으면 QuizNotFoundException 발생 (500)
     */
    private QuizPool getTodayQuiz() {
        LocalDate today = LocalDate.now(KST);

        DailyQuizSchedule schedule = scheduleRepository.findByQuizDate(today)
                .orElseThrow(() -> new QuizNotFoundException(
                        "오늘 출제된 데일리 퀴즈가 없습니다. 날짜: " + today
                ));

        return quizPoolRepository.findById(schedule.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException(
                        "퀴즈를 찾을 수 없습니다. quizId: " + schedule.getQuizId()
                ));
    }

    /**
     * 정답 비교 — 띄어쓰기 제거 + 대소문자 무시
     * (null 입력 안전)
     */
    private boolean isAnswerCorrect(String userAnswer, String realAnswer) {
        if (userAnswer == null || realAnswer == null) return false;
        String normalizedUser = userAnswer.replaceAll("\\s+", "");
        String normalizedReal = realAnswer.replaceAll("\\s+", "");
        return normalizedUser.equalsIgnoreCase(normalizedReal);
    }

    /**
     * 정답 점수 계산 (기능명세서 기준)
     *   - 기본 20점
     *   - 시도 보너스: 1회 +15, 2회 +10, 3회 +5, 4~5회 0
     *   - 힌트 사용 시 위 합계에서 5점 차감 (최소 0)
     */
    private int calculateScore(int attemptCount, boolean hintUsed) {
        int bonus = switch (attemptCount) {
            case 1 -> 15;
            case 2 -> 10;
            case 3 -> 5;
            default -> 0;
        };
        int total = BASE_CORRECT_SCORE + bonus;
        if (hintUsed) {
            total = Math.max(0, total - HINT_PENALTY);
        }
        return total;
    }
}
