package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.DailyQuizSchedule;
import com._ya.neoliz.domain.QuizAttempt;
import com._ya.neoliz.domain.QuizPool;
import com._ya.neoliz.domain.QuizSequenceItem;
import com._ya.neoliz.persistence.repository.DailyQuizScheduleRepository;
import com._ya.neoliz.persistence.repository.QuizAttemptRepository;
import com._ya.neoliz.persistence.repository.QuizPoolRepository;
import com._ya.neoliz.persistence.repository.QuizSequenceItemRepository;
import com._ya.neoliz.presentation.dto.response.DailyQuizResponse;
import com._ya.neoliz.presentation.dto.response.DailyQuizResponse.EmojiInfo;
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
     * @param userId 사용자 PK (현재는 RequestParam으로 받음, JWT 도입 시 토큰에서 추출)
     * @return 데일리 퀴즈 조회 응답 DTO
     */
    public DailyQuizResponse getDailyQuiz(Long userId) {
        // (1) 오늘 출제된 퀴즈 스케줄 찾기
        LocalDate today = LocalDate.now(KST);
        DailyQuizSchedule schedule = scheduleRepository.findByQuizDate(today)
                .orElseThrow(() -> new IllegalStateException(
                        "오늘 출제된 데일리 퀴즈가 없습니다. 날짜: " + today
                ));

        // (2) quiz_id로 퀴즈 본문 조회 (정답/카테고리 등)
        QuizPool quiz = quizPoolRepository.findById(schedule.getQuizId())
                .orElseThrow(() -> new IllegalStateException(
                        "퀴즈를 찾을 수 없습니다. quizId: " + schedule.getQuizId()
                ));

        // (3) 퀴즈의 이모지 시퀀스 조회 (순서 보장)
        List<QuizSequenceItem> items =
                quizSequenceItemRepository.findByQuizIdOrderByOrderIndexAsc(quiz.getId());

        // (4) QuizSequenceItem → EmojiInfo DTO 변환
        // TODO: 주희님의 Emoji 엔티티/EmojiRepository 머지되면 그때 imageUrl/soundUrl 채우기.
        //       지금은 emojiId만 채우고 url은 빈 문자열로 임시 처리.
        List<EmojiInfo> sequence = items.stream()
                .map(item -> EmojiInfo.builder()
                        .emojiId(item.getEmojiId())
                        .imageUrl("")
                        .soundUrl("")
                        .build())
                .toList();

        // (5) 사용자의 이 퀴즈 시도 기록 조회 (미시도면 Optional.empty())
        Optional<QuizAttempt> attemptOpt =
                quizAttemptRepository.findByUserIdAndQuizId(userId, quiz.getId());

        // (6) 응답 DTO 조립 후 반환 (DTO 안에서 case 1/2/3 분기 처리됨)
        return DailyQuizResponse.of(quiz, sequence, attemptOpt);
    }
}
