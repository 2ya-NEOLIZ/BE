package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.User;
import com._ya.neoliz.persistence.repository.ScoreLogRepository;
import com._ya.neoliz.persistence.repository.UserRepository;
import com._ya.neoliz.presentation.dto.response.WeeklyRankingResponse;
import com._ya.neoliz.presentation.dto.response.WeeklyRankingResponse.RankEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ScoreLogRepository scoreLogRepository;
    private final UserRepository userRepository;

    public WeeklyRankingResponse getWeeklyRanking(Long userId) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime weekEnd = today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59, 999999999);

        List<Object[]> scores = scoreLogRepository.findAllWeeklyScores(weekStart, weekEnd);

        List<RankEntry> top5 = new ArrayList<>();
        RankEntry me = null;
        boolean isInTop5 = false;

        for (int i = 0; i < scores.size(); i++) {
            Long rankUserId = ((Number) scores.get(i)[0]).longValue();
            int totalScore = ((Number) scores.get(i)[1]).intValue();
            int rank = i + 1;
            boolean isMeFlag = rankUserId.equals(userId);

            if (rank <= 5) {
                User user = userRepository.findById(rankUserId).orElse(null);
                if (user == null) continue;
                top5.add(RankEntry.builder()
                        .rank(rank)
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .totalScore(totalScore)
                        .isMe(isMeFlag)
                        .build());
            }

            if (isMeFlag) {
                if (rank <= 5) {
                    isInTop5 = true;
                } else {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        me = RankEntry.builder()
                                .rank(rank)
                                .nickname(user.getNickname())
                                .profileImageUrl(user.getProfileImageUrl())
                                .totalScore(totalScore)
                                .isMe(null)
                                .build();
                    }
                }
            }
        }

        return WeeklyRankingResponse.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .top5(top5)
                .me(isInTop5 ? null : me)
                .build();
    }
}
