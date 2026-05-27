package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.SequenceService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.request.SaveSequenceRequest;
import com._ya.neoliz.presentation.dto.response.SaveSequenceResponse;
import com._ya.neoliz.presentation.dto.response.SequenceDetailResponse;
import com._ya.neoliz.presentation.dto.response.SequencePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "시퀀스", description = "시퀀스 관련 API")
@RestController
@RequestMapping("/api/v1/neoliz/users")
@RequiredArgsConstructor
public class SequenceController {

    private final SequenceService sequenceService;

    // 1. 시퀀스 목록 조회
    @GetMapping("/me/sequences")
    @Operation(summary = "시퀀스 목록 조회", description = "마이페이지에서 사용자의 시퀀스 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<SequencePageResponse>> getSequence( @AuthenticationPrincipal Long userId, @ParameterObject @PageableDefault(size = 4, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        SequencePageResponse response = sequenceService.getSequenceList(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 완료", response));
    }

    // 2. 시퀀스 상세 조회
    @GetMapping("/me/sequences/{sequenceId}")
    @Operation(summary = "시퀀스 상세 조회", description = "마이페이지에서 사용자의 상세 시퀀스를 조회합니다.")
    public ResponseEntity<ApiResponse<SequenceDetailResponse>> getSequenceDetail(@AuthenticationPrincipal Long userId, @PathVariable("sequenceId") Long sequenceId) {
        SequenceDetailResponse response = sequenceService.getSequenceDetail(userId, sequenceId);
        return ResponseEntity.ok(ApiResponse.success("조회 완료", response));
    }

    // 3. 시퀀스 저장
    @PostMapping("/me/sequences")
    @Operation(summary = "시퀀스 저장", description = "사용자가 만든 이모지 시퀀스를 저장합니다.")
    public ResponseEntity<ApiResponse<SaveSequenceResponse>> saveSequence(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SaveSequenceRequest request) {
        SaveSequenceResponse response = sequenceService.saveSequence(userId, request);
        return ResponseEntity.ok(ApiResponse.success("시퀀스 저장 ㄴ성공", response));
    }

    // 4. 시퀀스 삭제
    @DeleteMapping("/me/sequences/{sequenceId}")
    @Operation(summary = "시퀀스 삭제", description = "마이페이지에서 사용자의 시퀀스를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteSequence(@AuthenticationPrincipal Long userId, @PathVariable("sequenceId") Long sequenceId) {
        sequenceService.deleteSequence(userId, sequenceId);
        return ResponseEntity.ok(ApiResponse.success("시퀀스 삭제 성공", null));
    }

}
