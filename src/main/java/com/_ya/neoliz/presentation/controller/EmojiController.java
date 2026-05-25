package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.EmojiService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.EmojiDetailResponse;
import com._ya.neoliz.presentation.dto.response.EmojiListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이모지", description = "이모지 관련 API")
@RestController
@RequestMapping("/api/v1/neoliz/emojis")
@RequiredArgsConstructor
public class EmojiController {

    private final EmojiService emojiService;

    @Operation(summary = "카테고리별 이모지 조회", description = "카테고리(animal/hufs/game/meme)에 속하는 이모지 12개를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<EmojiListResponse>> getEmojisByCategory(
            @RequestParam String category) {
        EmojiListResponse response = emojiService.getEmojisByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("이모지 조회 성공", response));
    }

    @Operation(summary = "이모지 단건 조회", description = "emojiId로 특정 이모지의 상세 정보를 조회합니다.")
    @GetMapping("/{emojiId}")
    public ResponseEntity<ApiResponse<EmojiDetailResponse>> getEmojiById(
            @PathVariable Long emojiId) {
        EmojiDetailResponse response = emojiService.getEmojiById(emojiId);
        return ResponseEntity.ok(ApiResponse.success("이모지 단건 조회 성공", response));
    }

}
