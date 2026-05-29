package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.RankingService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.WeeklyRankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "랭킹", description = "랭킹 관련 API")
@RestController
@RequestMapping("/api/v1/neoliz/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(
            summary = "주간 랭킹 TOP5 + 내 순위 조회",
            description = "이번 주(KST 월~일) 기준 TOP5와 본인 순위를 반환합니다.<br><br>" +
                    "<b>TOP5 안/밖 분기</b><br>" +
                    "  - TOP5 안: top5 배열의 본인 항목에 isMe: true, me 필드는 null<br>" +
                    "  - TOP5 밖: me 필드에 본인 순위 정보, top5의 isMe는 모두 false<br><br>" +
                    "<b>점수 체계</b><br>" +
                    "  - 일일 접속: 10점 (하루 1회)<br>" +
                    "  - 이모지 퀴즈: 정답 20점 + 시도 보너스 (1회 +15, 2회 +10, 3회 +5), 힌트 사용 시 -5점<br>" +
                    "  - 이모지 캐치: 판정 + 콤보 (최대 258점 / 하루 3판)<br>" +
                    "  - 시퀀스 저장: 15점 (하루 최대 3회)"
    )
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyRankingResponse>> getWeeklyRanking(
            @AuthenticationPrincipal Long userId) {
        WeeklyRankingResponse response = rankingService.getWeeklyRanking(userId);
        return ResponseEntity.ok(ApiResponse.success("주간 랭킹 조회 성공", response));
    }
}
