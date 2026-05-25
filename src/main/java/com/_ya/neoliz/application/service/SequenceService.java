package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.Sequence;
import com._ya.neoliz.domain.SequenceItem;
import com._ya.neoliz.global.exception.ForbiddenException;
import com._ya.neoliz.global.exception.SequenceNotFoundException;
import com._ya.neoliz.persistence.repository.SequenceItemRepository;
import com._ya.neoliz.persistence.repository.SequenceRepository;
import com._ya.neoliz.presentation.dto.response.SequenceDetailResponse;
import com._ya.neoliz.presentation.dto.response.SequenceItemResponse;
import com._ya.neoliz.presentation.dto.response.SequencePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SequenceService {
    private final SequenceRepository sequenceRepository;
    private final SequenceItemRepository sequenceItemRepository;
    public SequencePageResponse getSequenceList(Long id, Pageable pageable) {
        Page<Sequence> sequencePage = sequenceRepository.findAllByUserId(id, pageable);
        return SequencePageResponse.from(sequencePage);
    }

    public SequenceDetailResponse getSequenceDetail(Long userId, Long sequenceId) {
        Sequence sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new SequenceNotFoundException("조회 실패"));
        if (!sequence.getUserId().equals(userId)) {
            throw new ForbiddenException("권한 불일치");
        }
        List<SequenceItem> items = sequenceItemRepository.findBySequenceIdOrderByOrderIndexAsc(sequenceId);
        List<SequenceItemResponse> itemsResponses = items.stream()
                .map(item -> {
                    String soundUrl = "임시 Url";
                    String imageUrl = "임시 Url";
                    return SequenceItemResponse.from(item, soundUrl, imageUrl);
                })
                .toList();
        return SequenceDetailResponse.from(sequence, itemsResponses);
    }

    @Transactional
    public void deleteSequence(Long userId, Long sequenceId) {
        Sequence sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new SequenceNotFoundException("조회 실패")); // 404
        if (!sequence.getUserId().equals(userId)) {
            throw new ForbiddenException("권한 불일치"); // 403
        }
        sequenceItemRepository.deleteBySequenceId(sequenceId);
        sequenceRepository.delete(sequence);
    }

}
