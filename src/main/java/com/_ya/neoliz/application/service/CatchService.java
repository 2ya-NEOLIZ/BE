package com._ya.neoliz.application.service;

import com._ya.neoliz.persistence.repository.CatchGameResultRepository;
import com._ya.neoliz.presentation.dto.response.CatchStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 이모지 캐치 도메인 Service
 *
 * - 일일 플레이 가능 횟수 조회 / 게임 시작 / 결과 제출 비즈니스 로직 담당 예정
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

    private final CatchGameResultRepository catchGameResultRepository;

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
        LocalDate today = LocalDate.now(KST);
        LocalDateTime startOfDay = today.atStartOfDay();          // 오늘 00:00:00.000000000
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);     // 오늘 23:59:59.999999999

        int playedCount = catchGameResultRepository
                .countByUserIdAndPlayedAtBetween(userId, startOfDay, endOfDay);

        int remainingPlays = Math.max(0, MAX_PLAYS - playedCount);

        return CatchStatusResponse.of(remainingPlays, MAX_PLAYS);
    }
}
