package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.SequenceService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.SequencePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "시퀀스", description = "시퀀스 관련 API")
@RestController
@RequestMapping("/api/v1/neoliz/users")
@RequiredArgsConstructor
public class SequenceController {
    private final SequenceService sequenceService;
    @GetMapping("/me/sequences")
    @Operation(summary = "시퀀스 목록 조회", description = "마이페이지에서 사용자의 시퀀스 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<SequencePageResponse>> getSequence(@ParameterObject @PageableDefault(size = 4, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long id = 1L; // getSequenceList()를 위한 임시 데이터
        SequencePageResponse response = sequenceService.getSequenceList(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 완료", response));
    }
}
