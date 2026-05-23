package com._ya.neoliz.presentation.controller;

import com._ya.neoliz.application.service.SequenceService;
import com._ya.neoliz.global.response.ApiResponse;
import com._ya.neoliz.presentation.dto.response.SequencePageResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/neoliz/users")
@RequiredArgsConstructor
public class SequenceController {
    private final SequenceService sequenceService;
    @GetMapping("/me/sequences")
    public ResponseEntity<ApiResponse<SequencePageResponse>> getSequence(@ParameterObject @PageableDefault(size = 4, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long id = 1L; // getSequenceList()를 위한 임시 데이터
        SequencePageResponse response = sequenceService.getSequenceList(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 완료", response));
    }
}
